package org.example.userservice.dao;

import org.example.userservice.entity.User;
import org.example.userservice.test.AbstractDatabaseTest;
import org.example.userservice.util.HibernateUtil;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для UserDAO.
 * Проверяет все CRUD операции с базой данных.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDAOTest extends AbstractDatabaseTest {

    private static UserDAO userDAO;

    @BeforeAll
    static void setUp() {
        userDAO = new UserDAOImpl();
    }

    @BeforeEach
    void cleanBeforeTest() {
        clearTables();
    }

    @Test
    @Order(1)
    void testSaveUser() {
        User user = new User("Save Test User", "save.test@example.com", 25);
        User saved = userDAO.save(user);
        assertNotNull(saved.getId());
        assertEquals("Save Test User", saved.getName());
    }

    @Test
    @Order(2)
    void testFindById() {
        User user = new User("Find By ID User", "find.id@example.com", 30);
        User saved = userDAO.save(user);

        Optional<User> found = userDAO.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Find By ID User", found.get().getName());
    }

    @Test
    @Order(3)
    void testFindByEmail() {
        User user = new User("Find By Email User", "find.email@example.com", 28);
        User saved = userDAO.save(user);

        Optional<User> found = userDAO.findByEmail("find.email@example.com");
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    @Order(4)
    void testFindAll() {
        userDAO.save(new User("Find All User 1", "find.all1@example.com", 25));
        userDAO.save(new User("Find All User 2", "find.all2@example.com", 30));

        List<User> users = userDAO.findAll();
        assertEquals(2, users.size());
    }

    @Test
    @Order(5)
    void testUpdateUser() {
        User user = new User("Original Name", "update@example.com", 35);
        User saved = userDAO.save(user);

        saved.setName("Updated Name");
        saved.setAge(36);
        userDAO.update(saved);

        Optional<User> found = userDAO.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated Name", found.get().getName());
        assertEquals(36, found.get().getAge());
    }

    @Test
    @Order(6)
    void testExistsByEmail() {
        userDAO.save(new User("Exists Test User", "exists@example.com", 40));

        assertTrue(userDAO.existsByEmail("exists@example.com"));
        assertFalse(userDAO.existsByEmail("nonexistent@example.com"));
    }

    @Test
    @Order(7)
    void testDeleteUser() {
        User user = new User("Delete Test User", "delete@example.com", 45);
        User saved = userDAO.save(user);

        userDAO.delete(saved);

        assertFalse(userDAO.findById(saved.getId()).isPresent());
    }
}