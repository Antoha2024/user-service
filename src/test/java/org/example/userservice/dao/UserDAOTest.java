package org.example.userservice.dao;

import org.example.userservice.entity.User;
import org.example.userservice.util.HibernateUtil;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Модульные тесты для UserDAO.
 * Проверяют корректность работы всех CRUD операций с базой данных.
 * Использует реальную тестовую базу данных.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDAOTest {
    private static UserDAO userDAO;
    private static User testUser;

    /**
     * Инициализация перед всеми тестами.
     * Создает DAO объект и тестового пользователя.
     */
    @BeforeAll
    static void setUp() {
        userDAO = new UserDAOImpl();
        testUser = new User("Test User", "test@example.com", 25);
    }
    /**
     * Очистка после всех тестов.
     * Удаляет тестовые данные и закрывает SessionFactory.
     */
    @AfterAll
    static void tearDown() {
        userDAO.findByEmail("test@example.com").ifPresent(user -> userDAO.delete(user));
        HibernateUtil.shutdown();
    }
    /**
     * Тест сохранения нового пользователя.
     * Проверяет, что ID генерируется автоматически.
     */
    @Test
    @Order(1)
    void testSaveUser() {
        User saved = userDAO.save(testUser);
        assertNotNull(saved.getId());
        testUser.setId(saved.getId());
    }
    /**
     * Тест поиска пользователя по ID.
     * Проверяет, что найденный пользователь соответствует сохраненному.
     */
    @Test
    @Order(2)
    void testFindById() {
        Optional<User> found = userDAO.findById(testUser.getId());
        assertTrue(found.isPresent());
        assertEquals(testUser.getEmail(), found.get().getEmail());
    }
    /**
     * Тест поиска пользователя по email.
     * Проверяет уникальность email и корректность поиска.
     */
    @Test
    @Order(3)
    void testFindByEmail() {
        Optional<User> found = userDAO.findByEmail(testUser.getEmail());
        assertTrue(found.isPresent());
        assertEquals(testUser.getId(), found.get().getId());
    }
    /**
     * Тест получения всех пользователей.
     * Проверяет, что список не пустой (содержит тестового пользователя).
     */
    @Test
    @Order(4)
    void testFindAll() {
        List<User> users = userDAO.findAll();
        assertFalse(users.isEmpty());
    }
    /**
     * Тест обновления данных пользователя.
     * Проверяет, что изменения сохраняются в базе данных.
     */
    @Test
    @Order(5)
    void testUpdateUser() {
        testUser.setName("Updated Name");
        userDAO.update(testUser);

        Optional<User> found = userDAO.findById(testUser.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated Name", found.get().getName());
    }
    /**
     * Тест проверки существования пользователя по email.
     * Проверяет, что метод корректно определяет наличие пользователя.
     */
    @Test
    @Order(6)
    void testExistsByEmail() {
        boolean exists = userDAO.existsByEmail(testUser.getEmail());
        assertTrue(exists);
    }
    /**
     * Тест удаления пользователя.
     * Проверяет, что после удаления пользователь больше не находится в БД.
     */
    @Test
    @Order(7)
    void testDeleteUser() {
        userDAO.delete(testUser);
        Optional<User> found = userDAO.findById(testUser.getId());
        assertFalse(found.isPresent());
    }
}