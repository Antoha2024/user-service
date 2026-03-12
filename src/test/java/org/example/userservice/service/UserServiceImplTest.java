package org.example.userservice.service;

import org.example.userservice.dao.UserDAO;
import org.example.userservice.dto.UserDTO;
import org.example.userservice.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Тестирование Service-слоя
 */

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        testUser = new User("Test User", "test@example.com", 25);
        testUser.setId(1L);

        testUserDTO = new UserDTO();
        testUserDTO.setId(1L);
        testUserDTO.setName("Test User");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setAge(25);
    }

    /**
     * Проверяет успешную регистрацию нового пользователя
     */
    @Test
    void registerUser_Success() {
        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenReturn(testUser);

        UserDTO result = userService.registerUser(testUserDTO);

        assertNotNull(result);
        assertEquals("Test User", result.getName());
        verify(userDAO, times(1)).save(any(User.class));
    }

    /**
     * Регистрация с некорректным email
     */
    @Test
    void registerUser_InvalidEmail_ThrowsException() {
        testUserDTO.setEmail("invalid-email");

        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(testUserDTO);
        });

        verify(userDAO, never()).save(any(User.class));
    }

    @Test
    void registerUser_EmailAlreadyExists_ThrowsException() {
        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(RuntimeException.class, () -> {
            userService.registerUser(testUserDTO);
        });

        verify(userDAO, never()).save(any(User.class));
    }

    /**
     * Успешная проверка получения существующего пользователя по ID
     */
    @Test
    void getUserById_UserExists() {
        when(userDAO.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<UserDTO> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test User", result.get().getName());
        verify(userDAO, times(1)).findById(1L);
    }

    @Test
    void getUserById_UserNotFound() {
        when(userDAO.findById(99L)).thenReturn(Optional.empty());

        Optional<UserDTO> result = userService.getUserById(99L);

        assertFalse(result.isPresent());
        verify(userDAO, times(1)).findById(99L);
    }

    @Test
    void getUserByEmail_UserExists() {
        when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        Optional<UserDTO> result = userService.getUserByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals("Test User", result.get().getName());
        verify(userDAO, times(1)).findByEmail("test@example.com");
    }

    @Test
    void updateUser_Success() {
        // ИСПРАВЛЕНО: Настраиваем findById, чтобы он возвращал пользователя
        when(userDAO.findById(1L)).thenReturn(Optional.of(testUser));
        when(userDAO.update(any(User.class))).thenReturn(testUser);

        UserDTO result = userService.updateUser(testUserDTO);

        assertNotNull(result);
        assertEquals("Test User", result.getName());
        verify(userDAO, times(1)).findById(1L);
        verify(userDAO, times(1)).update(any(User.class));
    }

    @Test
    void updateUser_WithoutId_ThrowsException() {
        testUserDTO.setId(null);

        assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(testUserDTO);
        });

        verify(userDAO, never()).update(any(User.class));
    }

    /**
     *  Обновление несуществующего пользователя
     */
    @Test
    void updateUser_UserNotFound_ThrowsException() {
        when(userDAO.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            userService.updateUser(testUserDTO);
        });

        verify(userDAO, never()).update(any(User.class));
    }

    @Test
    void deleteUser_Success() {
        doNothing().when(userDAO).deleteById(1L);

        userService.deleteUser(1L);

        verify(userDAO, times(1)).deleteById(1L);
    }
}