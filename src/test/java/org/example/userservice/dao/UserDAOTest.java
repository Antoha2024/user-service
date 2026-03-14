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
 * Каждый тест создает свои собственные данные для изоляции.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
public class UserDAOTest {
    private static UserDAO userDAO;

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
    }

    /**
     * Очистка таблицы перед каждым тестом для изоляции.
     * Каждый тест начинает с чистой базы данных.
     */
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

    /**
     * Тест сохранения нового пользователя.
     * Проверяет, что ID генерируется автоматически.
     */
    @Test
    @Order(1)
    void testSaveUser() {
        // given
        User user = new User("Save Test User", "save.test@example.com", 25);

        // when
        User saved = userDAO.save(user);

        // then
        assertNotNull(saved.getId());
        assertEquals("Save Test User", saved.getName());
        assertEquals("save.test@example.com", saved.getEmail());
        assertEquals(25, saved.getAge());
    }

    /**
     * Тест поиска пользователя по ID.
     * Проверяет, что найденный пользователь соответствует сохраненному.
     */
    @Test
    @Order(2)
    void testFindById() {
        // given - создаем пользователя для этого теста
        User user = new User("Find By ID User", "find.id@example.com", 30);
        User saved = userDAO.save(user);

        // when
        Optional<User> found = userDAO.findById(saved.getId());

        // then
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("Find By ID User", found.get().getName());
        assertEquals("find.id@example.com", found.get().getEmail());
    }

    /**
     * Тест поиска пользователя по email.
     * Проверяет уникальность email и корректность поиска.
     */
    @Test
    @Order(3)
    void testFindByEmail() {
        // given - создаем пользователя для этого теста
        User user = new User("Find By Email User", "find.email@example.com", 28);
        User saved = userDAO.save(user);

        // when
        Optional<User> found = userDAO.findByEmail("find.email@example.com");

        // then
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("Find By Email User", found.get().getName());
    }

    /**
     * Тест получения всех пользователей.
     * Проверяет, что список содержит созданных пользователей.
     */
    @Test
    @Order(4)
    void testFindAll() {
        // given - создаем несколько пользователей для этого теста
        User user1 = new User("Find All User 1", "find.all1@example.com", 25);
        User user2 = new User("Find All User 2", "find.all2@example.com", 30);
        userDAO.save(user1);
        userDAO.save(user2);

        // when
        List<User> users = userDAO.findAll();

        // then
        assertFalse(users.isEmpty());
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("find.all1@example.com")));
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("find.all2@example.com")));
    }

    /**
     * Тест обновления данных пользователя.
     * Проверяет, что изменения сохраняются в базе данных.
     */
    @Test
    @Order(5)
    void testUpdateUser() {
        // given - создаем пользователя для этого теста
        User user = new User("Original Name", "update@example.com", 35);
        User saved = userDAO.save(user);

        // when - обновляем данные
        saved.setName("Updated Name");
        saved.setAge(36);
        userDAO.update(saved);

        // then - проверяем, что изменения применились
        Optional<User> found = userDAO.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated Name", found.get().getName());
        assertEquals(36, found.get().getAge());
        assertEquals("update@example.com", found.get().getEmail()); // email не изменился
    }

    /**
     * Тест проверки существования пользователя по email.
     * Проверяет, что метод корректно определяет наличие пользователя.
     */
    @Test
    @Order(6)
    void testExistsByEmail() {
        // given - создаем пользователя для этого теста
        User user = new User("Exists Test User", "exists@example.com", 40);
        userDAO.save(user);

        // when & then
        boolean exists = userDAO.existsByEmail("exists@example.com");
        assertTrue(exists);

        boolean notExists = userDAO.existsByEmail("nonexistent@example.com");
        assertFalse(notExists);
    }

    /**
     * Тест удаления пользователя.
     * Проверяет, что после удаления пользователь больше не находится в БД.
     */
    @Test
    @Order(7)
    void testDeleteUser() {
        // given - создаем пользователя для этого теста
        User user = new User("Delete Test User", "delete@example.com", 45);
        User saved = userDAO.save(user);

        // when
        userDAO.delete(saved);

        // then
        Optional<User> found = userDAO.findById(saved.getId());
        assertFalse(found.isPresent());
    }
}