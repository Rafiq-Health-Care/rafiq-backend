package com.nexaworks.rafiq.config;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.type.StandardBasicTypes;

public class PostgreSQLFunctionContributor implements FunctionContributor {

    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {
        var functionRegistry = functionContributions.getFunctionRegistry();
        var typeConfiguration = functionContributions.getTypeConfiguration();

        functionRegistry.registerPattern("ts_match", "?1 @@ plainto_tsquery('english', ?2)",
                typeConfiguration.getBasicTypeRegistry().resolve(StandardBasicTypes.BOOLEAN));

        functionRegistry.registerPattern("similarity", "similarity(?1, ?2)",
                typeConfiguration.getBasicTypeRegistry().resolve(StandardBasicTypes.DOUBLE));
    }
}
