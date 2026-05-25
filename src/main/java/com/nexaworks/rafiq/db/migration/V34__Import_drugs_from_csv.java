package com.nexaworks.rafiq.db.migration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class V34__Import_drugs_from_csv extends BaseJavaMigration {

    private static final UUID SYSTEM_USER_ID = UUID
            .fromString("00000000-0000-0000-0000-000000000001");
    private static final int BATCH_SIZE = 1000;
    private static final String CSV_PATH = "static/drugs-egy.csv";

    @Override
    public void migrate(Context context) throws Exception {
        log.info("Starting drug data import from CSV...");

        Connection connection = context.getConnection();
        // NOTE: Flyway manages transactions - do not set autoCommit manually

        int imported = 0;
        int failed = 0;

        try {
            InputStream inputStream = loadCsvResource();

            try (inputStream;
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                String header = reader.readLine();
                if (header == null) {
                    log.warn("CSV file is empty");
                    return;
                }

                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        processCsvLine(connection, line);
                        imported++;

                        if (imported % BATCH_SIZE == 0) {
                            log.info("Imported {} drugs...", imported);
                            // Flyway manages the transaction, no manual commit needed
                        }
                    } catch (Exception e) {
                        failed++;
                        log.error("Error processing line (total failures: {}): {}", failed,
                                truncateString(line, 100), e);
                    }
                }

                // Flyway will commit the transaction automatically on success
                log.info("Successfully imported {} drugs from CSV ({} failed)", imported, failed);
            }
        } catch (Exception e) {
            // Flyway will roll back automatically on exception
            log.error("Error during CSV import", e);
            throw e;
        }
    }

    private InputStream loadCsvResource() {
        // Try Thread context classloader first (most reliable in-app servers)
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(CSV_PATH);

        if (inputStream != null) {
            log.info("Loaded CSV using Thread context classloader: {}", CSV_PATH);
            return inputStream;
        }

        // Fallback to class classloader
        inputStream = getClass().getClassLoader().getResourceAsStream(CSV_PATH);
        if (inputStream != null) {
            log.info("Loaded CSV using class classloader: {}", CSV_PATH);
            return inputStream;
        }

        log.error("CSV file not found in classpath: {}", CSV_PATH);
        throw new IllegalStateException("CSV file not found: " + CSV_PATH);
    }

    private void processCsvLine(Connection connection, String line) throws SQLException {
        String[] fields = parseCsvLine(line);

        if (fields.length < 11) {
            log.warn("Skipping invalid line with {} fields", fields.length);
            return;
        }

        String activeIngredientsStr = cleanField(fields[0]);
        String companyStr = cleanField(fields[1]);
        String form = cleanField(fields[3]);
        String drugGroup = cleanField(fields[4]);
        String price = cleanField(fields[6]);
        String pharmacology = cleanField(fields[7]);
        String route = cleanField(fields[8]);
        String tradeName = cleanField(fields[9]);

        if (tradeName == null || tradeName.isEmpty()) {
            return;
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());

        UUID drugId = getOrCreateDrug(connection, tradeName, drugGroup, form, route, price,
                pharmacology, now);

        if (companyStr != null && !companyStr.isEmpty()) {
            for (String companyName : companyStr.split(">")) {
                companyName = companyName.trim();
                if (!companyName.isEmpty()) {
                    UUID companyId = getOrCreateCompany(connection, companyName, now);
                    linkDrugToCompany(connection, drugId, companyId);
                }
            }
        }

        if (activeIngredientsStr != null && !activeIngredientsStr.isEmpty()) {
            for (String ingredientName : activeIngredientsStr.split("\\+")) {
                ingredientName = ingredientName.trim();
                if (!ingredientName.isEmpty()) {
                    UUID ingredientId = getOrCreateActiveIngredient(connection, ingredientName,
                            now);
                    linkDrugToActiveIngredient(connection, drugId, ingredientId);
                }
            }
        }
    }

    private String[] parseCsvLine(String line) {
        java.util.List<String> fields = new java.util.ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentField = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                // Handle escaped quotes: "" becomes "
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i++; // Skip next quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        fields.add(currentField.toString());

        return fields.toArray(new String[0]);
    }

    private String cleanField(String field) {
        if (field == null) {
            return null;
        }
        field = field.trim();
        if (field.startsWith("\"") && field.endsWith("\"") && field.length() > 1) {
            field = field.substring(1, field.length() - 1);
        }
        return field.isEmpty() ? null : field;
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        log.warn("Truncating value from {} to {} characters: {}...", value.length(), maxLength,
                truncateString(value, 50));
        return value.substring(0, maxLength);
    }

    private String truncateString(String str, int maxLength) {
        return str.length() <= maxLength ? str : str.substring(0, maxLength) + "...";
    }

    private UUID getOrCreateDrug(Connection connection, String tradeName, String drugGroup,
            String form, String route, String p, String pharmacology, Timestamp now)
            throws SQLException {
        String selectSql = "SELECT id FROM drug WHERE trade_name = ?";
        double price;
        try {
            price = Double.parseDouble(p);
        } catch (Exception e) {
            log.warn("Invalid price: {}", p);
            price = 0.0;
        }
        try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
            stmt.setString(1, truncate(tradeName, 255));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return UUID.fromString(rs.getString("id"));
            }
        }

        UUID drugId = UUID.randomUUID();
        String insertSql = """
                INSERT INTO drug (id, trade_name, drug_group, dosage_form, route, price, pharmacology,
                                created_at, created_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
            stmt.setObject(1, drugId);
            stmt.setString(2, truncate(tradeName, 255));
            stmt.setString(3, truncate(drugGroup, 255));
            stmt.setString(4, truncate(form, 255));
            stmt.setString(5, truncate(route, 255));
            stmt.setDouble(6, price);
            stmt.setString(7, pharmacology);
            stmt.setTimestamp(8, now);
            stmt.setObject(9, SYSTEM_USER_ID);
            stmt.executeUpdate();
        }

        return drugId;
    }

    private UUID getOrCreateCompany(Connection connection, String name, Timestamp now)
            throws SQLException {
        String selectSql = "SELECT id FROM company WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
            stmt.setString(1, truncate(name, 255));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return UUID.fromString(rs.getString("id"));
            }
        }

        UUID companyId = UUID.randomUUID();
        String insertSql = "INSERT INTO company (id, name, created_at, created_by) VALUES (?, ?, ?, ?)";
        return getUuid(connection, name, now, companyId, insertSql);
    }

    private UUID getUuid(Connection connection, String name, Timestamp now, UUID companyId,
            String insertSql) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
            stmt.setObject(1, companyId);
            stmt.setString(2, truncate(name, 255));
            stmt.setTimestamp(3, now);
            stmt.setObject(4, SYSTEM_USER_ID);
            stmt.executeUpdate();
        }

        return companyId;
    }

    private UUID getOrCreateActiveIngredient(Connection connection, String name, Timestamp now)
            throws SQLException {
        String selectSql = "SELECT id FROM active_ingredient WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
            stmt.setString(1, truncate(name, 255));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return UUID.fromString(rs.getString("id"));
            }
        }

        UUID ingredientId = UUID.randomUUID();
        String insertSql = "INSERT INTO active_ingredient (id, name, created_at, created_by) VALUES (?, ?, ?, ?)";
        return getUuid(connection, name, now, ingredientId, insertSql);
    }

    private void linkDrugToCompany(Connection connection, UUID drugId, UUID companyId)
            throws SQLException {
        String checkSql = "SELECT 1 FROM drug_company WHERE drug_id = ? AND company_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(checkSql)) {
            stmt.setObject(1, drugId);
            stmt.setObject(2, companyId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return;
            }
        }

        String insertSql = "INSERT INTO drug_company (drug_id, company_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
            stmt.setObject(1, drugId);
            stmt.setObject(2, companyId);
            stmt.executeUpdate();
        }
    }

    private void linkDrugToActiveIngredient(Connection connection, UUID drugId, UUID ingredientId)
            throws SQLException {
        String checkSql = "SELECT 1 FROM drug_active_ingredient WHERE drug_id = ? AND active_ingredient_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(checkSql)) {
            stmt.setObject(1, drugId);
            stmt.setObject(2, ingredientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return;
            }
        }

        String insertSql = "INSERT INTO drug_active_ingredient (drug_id, active_ingredient_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
            stmt.setObject(1, drugId);
            stmt.setObject(2, ingredientId);
            stmt.executeUpdate();
        }
    }
}