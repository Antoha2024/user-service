package org.example.userservice.dao;

import org.example.userservice.dto.UserDTO;
import org.example.userservice.entity.User;
import org.example.userservice.util.HibernateUtil;
import org.junit.jupiter.api.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для проверки маппинга между Entity и DTO через DAO слой.
 * Фокус на том, что технические поля (createdAt, updatedAt) не должны быть видны в DTO.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDTOMappingTest {
    private static UserDAO userDAO;
    private static User savedUser;

    @BeforeAll
    static void setUp() {
        userDAO = new UserDAOImpl();
    }

    @BeforeEach
    void cleanAndSetup() {

        try {
            userDAO.findByEmail("dto.test@example.com").ifPresent(user -> userDAO.delete(user));
            userDAO.findByEmail("noid@example.com").ifPresent(user -> userDAO.delete(user));
            userDAO.findByEmail("update@example.com").ifPresent(user -> userDAO.delete(user));
        } catch (Exception e) {

        }
    }

    @AfterAll
    static void tearDown() {

    }

    /**
     * Тест сохранения и получения пользователя через DTO.
     * Проверяет, что технические поля (кроме ID) не просачиваются в DTO.
     */
    @Test
    @Order(1)
    void testUserDtoDoesNotContainTechnicalFields() {

        User user = new User("DTO Test User", "dto.test@example.com", 30);
        savedUser = userDAO.save(user);
        assertNotNull(savedUser.getId(), "Пользователь должен сохраниться с ID");

        Optional<User> found = userDAO.findById(savedUser.getId());
        assertTrue(found.isPresent(), "Пользователь должен находиться по ID");

        UserDTO dto = convertToDto(found.get());

        assertAll("Бизнес-поля должны маппиться корректно",
            () -> assertEquals("DTO Test User", dto.getName()),
            () -> assertEquals("dto.test@example.com", dto.getEmail()),
            () -> assertEquals(30, dto.getAge())
        );

        assertNotNull(dto.getId(), "DTO должно содержать ID");

        assertAll("Технические поля не должны быть в DTO",
            () -> assertFalse(hasField(UserDTO.class, "createdAt"), "DTO не должно содержать поле createdAt"),
            () -> assertFalse(hasField(UserDTO.class, "updatedAt"), "DTO не должно содержать поле updatedAt"),
            () -> assertFalse(hasField(UserDTO.class, "version"), "DTO не должно содержать поле version")
        );
    }

    /**
     * Тест проверки, что ID генерируется БД.
     */
    @Test
    @Order(2)
    void testIdIsGeneratedByDatabase() {

        User user = new User("No ID User", "noid@example.com", 25);
        User saved = userDAO.save(user);

        assertNotNull(saved.getId(), "ID должен быть сгенерирован БД");
        assertTrue(saved.getId() > 0, "ID должен быть положительным числом");

        UserDTO dto = convertToDto(saved);
        assertNotNull(dto.getId(), "DTO должно содержать ID");
        assertEquals(saved.getId(), dto.getId(), "ID в DTO должен совпадать с ID в Entity");

        userDAO.delete(saved);
    }

    /**
     * Тест обновления пользователя и проверка через DTO.
     */
    @Test
    @Order(3)
    void testUpdateUserAndCheckDto() {

        User user = new User("Update Test", "update@example.com", 28);
        savedUser = userDAO.save(user);

        savedUser.setName("Updated Name");
        savedUser.setAge(29);
        userDAO.update(savedUser);

        Optional<User> found = userDAO.findById(savedUser.getId());
        assertTrue(found.isPresent());

        UserDTO dto = convertToDto(found.get());

        assertEquals("Updated Name", dto.getName());
        assertEquals("update@example.com", dto.getEmail());
        assertEquals(29, dto.getAge());

        assertNotNull(dto.getId(), "DTO должно содержать ID");
    }

    /**
     * Вспомогательный метод для конвертации User в UserDTO.
     */
    private UserDTO convertToDto(User user) {
        if (user == null) return null;
        
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setAge(user.getAge());
        return dto;
    }

    /**
     * Вспомогательный метод для проверки наличия поля в классе.
     */
    private boolean hasField(Class<?> clazz, String fieldName) {
        try {
            clazz.getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }
}