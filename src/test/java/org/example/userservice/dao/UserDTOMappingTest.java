package org.example.userservice.dao;

import org.example.userservice.dto.UserDTO;
import org.example.userservice.entity.User;
import org.example.userservice.test.AbstractDatabaseTest;
import org.junit.jupiter.api.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для проверки маппинга между Entity и DTO.
 * Проверяет, что технические поля не попадают в DTO.
 */

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDTOMappingTest extends AbstractDatabaseTest {

    private static UserDAO userDAO;

    @BeforeAll
    static void setUp() {
        userDAO = new UserDAOImpl();
    }

    @BeforeEach
    void cleanAndSetup() {
        clearTables();
    }

    private UserDTO convertToDto(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setAge(user.getAge());
        return dto;
    }

    private boolean hasField(Class<?> clazz, String fieldName) {
        try {
            clazz.getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    @Test
    @Order(1)
    void testUserDtoDoesNotContainTechnicalFields() {
        User user = new User("DTO Technical Fields Test", "dto.technical@example.com", 30);
        User savedUser = userDAO.save(user);

        UserDTO dto = convertToDto(savedUser);

        assertAll("Бизнес-поля должны маппиться корректно",
                () -> assertEquals("DTO Technical Fields Test", dto.getName()),
                () -> assertEquals("dto.technical@example.com", dto.getEmail()),
                () -> assertEquals(30, dto.getAge())
        );

        assertNotNull(dto.getId());

        assertAll("Технические поля не должны быть в DTO",
                () -> assertFalse(hasField(UserDTO.class, "createdAt")),
                () -> assertFalse(hasField(UserDTO.class, "updatedAt")),
                () -> assertFalse(hasField(UserDTO.class, "version"))
        );
    }

    @Test
    @Order(2)
    void testIdIsGeneratedByDatabase() {
        User user = new User("ID Generation Test", "id.gen@example.com", 25);
        User saved = userDAO.save(user);

        assertNotNull(saved.getId());
        assertTrue(saved.getId() > 0);

        UserDTO dto = convertToDto(saved);
        assertNotNull(dto.getId());
        assertEquals(saved.getId(), dto.getId());
    }

    @Test
    @Order(3)
    void testUpdateUserAndCheckDto() {
        User user = new User("Update DTO Test", "dto.update@example.com", 28);
        User savedUser = userDAO.save(user);

        savedUser.setName("Updated DTO Name");
        savedUser.setAge(29);
        userDAO.update(savedUser);

        UserDTO dto = convertToDto(savedUser);

        assertEquals("Updated DTO Name", dto.getName());
        assertEquals("dto.update@example.com", dto.getEmail());
        assertEquals(29, dto.getAge());
        assertNotNull(dto.getId());
    }
}