package org.example.userservice.test;

import org.example.userservice.util.HibernateUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class AbstractDatabaseTest {

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName
            .parse("postgres:15-alpine")
            .asCompatibleSubstituteFor("postgres");

    // ОДИН контейнер для ВСЕХ тестов
    private static PostgreSQLContainer<?> postgres;

    @BeforeAll
    static void setupDatabase() {
        if (postgres == null) {
            postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE)
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");
            postgres.start();
        }

        System.setProperty("hibernate.connection.url", postgres.getJdbcUrl());
        System.setProperty("hibernate.connection.username", postgres.getUsername());
        System.setProperty("hibernate.connection.password", postgres.getPassword());
        System.setProperty("hibernate.hbm2ddl.auto", "create-drop");

        System.out.println("Test database started at: " + postgres.getJdbcUrl());
    }

    @AfterAll
    static void cleanupDatabase() {
        // Не закрываем контейнер здесь!
        System.out.println("All tests finished. Container will be stopped by JVM.");
    }

    protected void clearTables() {
        HibernateUtil.doInTransaction(session -> {
            session.createQuery("DELETE FROM User").executeUpdate();
        });
    }
}