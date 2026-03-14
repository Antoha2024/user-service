package org.example.userservice.dao;

import org.example.userservice.dto.UserDTO;
import org.example.userservice.entity.User;
import org.example.userservice.util.HibernateUtil;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
public class UserDTOMappingTest {
    private static UserDAO userDAO;

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName
            .parse("postgres:15-alpine")
            .asCompatibleSubstituteFor("postgres");

    // ВОТ ЭТА АННОТАЦИЯ БЫЛА ПРОПУЩЕНА!
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

    @BeforeEach
    void cleanAndSetup() {
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
    void testUserDtoDoesNotContainTechnicalFields() {
        User user = new User("DTO Technical Fields Test", "dto.technical@example.com", 30);
        User savedUser = userDAO.save(user);
        assertNotNull(savedUser.getId());

        Optional<User> found = userDAO.findById(savedUser.getId());
        assertTrue(found.isPresent());

        UserDTO dto = convertToDto(found.get());

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

        Optional<User> found = userDAO.findById(savedUser.getId());
        assertTrue(found.isPresent());

        UserDTO dto = convertToDto(found.get());

        assertEquals("Updated DTO Name", dto.getName());
        assertEquals("dto.update@example.com", dto.getEmail());
        assertEquals(29, dto.getAge());
        assertNotNull(dto.getId());
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
}