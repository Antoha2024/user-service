package org.example.userservice.service;

import org.example.userservice.dto.UserDTO;
import java.util.Optional;

/**
 * Интерфейс сервиса для работы с пользователями.
 * Определяет бизнес-логику приложения.
 */
public interface UserService {
    /**
     * Регистрация нового пользователя
     */
    UserDTO registerUser(UserDTO userDTO);
    
    /**
     * Поиск пользователя по ID
     */
    Optional<UserDTO> getUserById(Long id);
    
    /**
     * Поиск пользователя по email
     */
    Optional<UserDTO> getUserByEmail(String email);
    
    /**
     * Обновление данных пользователя
     */
    UserDTO updateUser(UserDTO userDTO);
    
    /**
     * Удаление пользователя
     */
    void deleteUser(Long id);
}