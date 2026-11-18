package com.rit.researchflow.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Startup check that MariaDB is reachable via the configured DataSource.
 * Does not log credentials or connection passwords.
 */
@Component
public class DatabaseConnectivityVerifier implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectivityVerifier.class);

    private final DataSource dataSource;

    public DatabaseConnectivityVerifier(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT 1")) {
            if (!resultSet.next()) {
                throw new IllegalStateException("MariaDB connectivity check failed: SELECT 1 returned no row");
            }
            String product = connection.getMetaData().getDatabaseProductName();
            String catalog = connection.getCatalog();
            log.info("MariaDB connectivity verified (product={}, catalog={})", product, catalog);
        }
    }
}
