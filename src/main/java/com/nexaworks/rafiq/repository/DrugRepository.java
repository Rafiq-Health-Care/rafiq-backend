package com.nexaworks.rafiq.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nexaworks.rafiq.entities.Drug;

public interface DrugRepository extends JpaRepository<Drug, UUID> {
    @Query(value = """
            SELECT *
            FROM drug
            WHERE search_vector @@ plainto_tsquery('english',:drugName)
            OR trade_name ILIKE CONCAT('%',:drugName,'%')""", nativeQuery = true)
    Page<Drug> searchByFullText(String drugName, Pageable pageable);
}
