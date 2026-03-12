package org.example.userservice.service;

import org.example.userservice.dao.UserDAO;
import org.example.userservice.dto.UserDTO;
import org.example.userservice.entity.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * Реализация сервиса для работы с пользователями.
 * Содержит бизнес-логику приложения.
 * Взаимодействие с БД делегируется DAO-слою (интеграционная часть).
 */
public class UserServiceImpl implements UserService {
    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);
    
    private final UserDAO userDAO;
    
    /**
     * Конструктор с внедрением зависимости DAO.
     */
    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
    
    @Override
    public UserDTO registerUser(UserDTO userDTO) {
        logger.debug("Registering new user with email: {}", userDTO.getEmail());

        if (userDTO.getEmail() == null || !userDTO.getEmail().contains("@")) {
            logger.error("Invalid email format: {}", userDTO.getEmail());
            throw new IllegalArgumentException("Invalid email format");
        }

        if (userDAO.findByEmail(userDTO.getEmail()).isPresent()) {
            logger.error("User with email {} already exists", userDTO.getEmail());
            throw new RuntimeException("User with this email already exists");
        }

        User user = convertToEntity(userDTO);
        User saved = userDAO.save(user);
        logger.info("User registered successfully with ID: {}", saved.getId());
        
        return convertToDto(saved);
    }
    
    @Override
    public Optional<UserDTO> getUserById(Long id) {
        logger.debug("Finding user by ID: {}", id);
        return userDAO.findById(id).map(this::convertToDto);
    }
    
    @Override
    public Optional<UserDTO> getUserByEmail(String email) {
        logger.debug("Finding user by email: {}", email);
        return userDAO.findByEmail(email).map(this::convertToDto);
    }
    
    @Override
    public UserDTO updateUser(UserDTO userDTO) {
        logger.debug("Updating user with ID: {}", userDTO.getId());
        
        if (userDTO.getId() == null) {
            logger.error("Update attempted without ID");
            throw new IllegalArgumentException("ID is required for update");
        }

        Optional<User> existingUser = userDAO.findById(userDTO.getId());
        if (!existingUser.isPresent()) {
            logger.error("User not found with ID: {}", userDTO.getId());
            throw new RuntimeException("User not found with ID: " + userDTO.getId());
        }

        User user = existingUser.get();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setAge(userDTO.getAge());
        
        User updated = userDAO.update(user);
        logger.info("User updated successfully with ID: {}", updated.getId());
        
        return convertToDto(updated);
    }
    
    @Override
    public void deleteUser(Long id) {
        logger.debug("Deleting user with ID: {}", id);
        userDAO.deleteById(id);
        logger.info("User deleted successfully with ID: {}", id);
    }
    
    /**
     * Преобразование DTO в сущность
     */
    private User convertToEntity(UserDTO dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setAge(dto.getAge());
        return user;
    }
    
    /**
     * Преобразование сущности в DTO
     */
    private UserDTO convertToDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setAge(user.getAge());
        return dto;
    }
}