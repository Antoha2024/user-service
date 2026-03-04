package org.example.userservice.dao;

import org.example.userservice.entity.User;
import org.example.userservice.util.HibernateUtil;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDAOTest {
    private static UserDAO userDAO;
    private static User testUser;

    @BeforeAll
    static void setUp() {
        userDAO = new UserDAOImpl();
        testUser = new User("Test User", "test@example.com", 25);
    }

    @AfterAll
    static void tearDown() {
        // Clean up test data
        userDAO.findByEmail("test@example.com").ifPresent(user -> userDAO.delete(user));
        HibernateUtil.shutdown();
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
    }

    @Test
    @Order(7)
    void testDeleteUser() {
        userDAO.delete(testUser);
        Optional<User> found = userDAO.findById(testUser.getId());
        assertFalse(found.isPresent());
    }
}