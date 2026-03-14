package org.example.userservice.dao;

import org.example.userservice.entity.User;
import org.example.userservice.util.HibernateUtil;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Модульные тесты для UserDAO.
 * Использует Testcontainers для автоматического управления тестовой БД.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
public class UserDAOTest {
    private static UserDAO userDAO;
    private static User testUser;

    // ФИНАЛЬНЫЕ НАСТРОЙКИ ДЛЯ WINDOWS
    static {
        // Отключаем Ryuk
        System.setProperty("testcontainers.ryuk.disabled", "true");

        // Явно указываем использовать HTTP стратегию
        System.setProperty("testcontainers.dockerclient.strategy", "http");

        // Указываем хост Docker
        System.setProperty("docker.host", "tcp://localhost:2375");

        // Добавляем таймауты
        System.setProperty("testcontainers.http.connectTimeout", "120000");
        System.setProperty("testcontainers.http.readTimeout", "120000");

        // Включаем подробное логирование
        System.setProperty("org.testcontainers", "DEBUG");

        System.out.println("=== Testcontainers Windows Configuration ===");
        System.out.println("DOCKER_HOST: " + System.getProperty("docker.host"));
        System.out.println("Strategy: " + System.getProperty("testcontainers.dockerclient.strategy"));
        System.out.println("=============================================");
    }

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName
            .parse("postgres:15-alpine")
            .asCompatibleSubstituteFor("postgres");

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE)
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @BeforeAll
    static void setUp() {
        System.setProperty("hibernate.connection.url", postgres.getJdbcUrl());
        System.setProperty("hibernate.connection.username", postgres.getUsername());
        System.setProperty("hibernate.connection.password", postgres.getPassword());
        System.setProperty("hibernate.hbm2ddl.auto", "create-drop");

        userDAO = new UserDAOImpl();
        testUser = new User("Test User", "test@example.com", 25);
    }

    @BeforeEach
    void cleanBeforeTest() {
        HibernateUtil.doInTransaction(session -> {
            session.createQuery("DELETE FROM User").executeUpdate();
        });
    }

    @AfterAll
    static void tearDown() {
        // Testcontainers автоматически остановит контейнер
    }

    @Test
    @Order(1)
    void testSaveUser() {
        User saved = userDAO.save(testUser);
        assertNotNull(saved.getId());
        testUser.setId(saved.getId());
    }

    @Test
    @Order(2)
    void testFindById() {
        Optional<User> found = userDAO.findById(testUser.getId());
        assertTrue(found.isPresent());
        assertEquals(testUser.getEmail(), found.get().getEmail());
    }

    @Test
    @Order(3)
    void testFindByEmail() {
        Optional<User> found = userDAO.findByEmail(testUser.getEmail());
        assertTrue(found.isPresent());
        assertEquals(testUser.getId(), found.get().getId());
    }

    @Test
    @Order(4)
    void testFindAll() {
        List<User> users = userDAO.findAll();
        assertFalse(users.isEmpty());
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals(testUser.getEmail())));
    }

    @Test
    @Order(5)
    void testUpdateUser() {
        testUser.setName("Updated Name");
        userDAO.update(testUser);

        Optional<User> found = userDAO.findById(testUser.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated Name", found.get().getName());
    }

    @Test
    @Order(6)
    void testExistsByEmail() {
        boolean exists = userDAO.existsByEmail(testUser.getEmail());
        assertTrue(exists);
        boolean notExists = userDAO.existsByEmail("nonexistent@example.com");
        assertFalse(notExists);
    }

    @Test
    @Order(7)
    void testDeleteUser() {
        userDAO.delete(testUser);
        Optional<User> found = userDAO.findById(testUser.getId());
        assertFalse(found.isPresent());
    }
}