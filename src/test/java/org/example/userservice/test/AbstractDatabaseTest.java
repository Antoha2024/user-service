package org.example.userservice.test;

import org.example.userservice.util.HibernateUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Абстрактный базовый класс для всех тестов, требующих реальной базы данных.
 * Содержит общий контейнер PostgreSQL, который запускается один раз
 * для всех тестовых классов-наследников.
 */
@Testcontainers
public abstract class AbstractDatabaseTest {

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName
            .parse("postgres:15-alpine")
            .asCompatibleSubstituteFor("postgres");

    @Container
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE)
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @BeforeAll
    static void setupDatabase() {
        // Убеждаемся, что контейнер запущен
        if (!postgres.isRunning()) {
            postgres.start();
        }

        // Перенаправляем Hibernate на контейнер Testcontainers
        System.setProperty("hibernate.connection.url", postgres.getJdbcUrl());
        System.setProperty("hibernate.connection.username", postgres.getUsername());
        System.setProperty("hibernate.connection.password", postgres.getPassword());
        System.setProperty("hibernate.hbm2ddl.auto", "create-drop");

        System.out.println("Test database started at: " + postgres.getJdbcUrl());
    }

    @AfterAll
    static void cleanupDatabase() {
        System.out.println("All tests finished. Container will be stopped by Testcontainers.");
    }

    /**
     * Вспомогательный метод для очистки таблиц между тестами.
     * Каждый тестовый класс может вызывать его в @BeforeEach.
     */
    protected void clearTables() {
        HibernateUtil.doInTransaction(session -> {
            session.createQuery("DELETE FROM User").executeUpdate();
        });
    }
}