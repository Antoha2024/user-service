package org.example.userservice.dao;

import org.example.userservice.dto.UserDTO;
import org.example.userservice.entity.User;
import java.util.List;
import java.util.Optional;
/**
 * Интерфейс Data Access Object (DAO) для управления пользователями.
 * Определяет контракт для операций с базой данных, следуя паттерну Repository.
 * Все методы должны быть реализованы конкретным DAO классом.
 */ 
public interface UserDAO {
    /**
     * CRUD операции с сущностями
     */
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    List<User> findByName(String name);
    User update(User user);
    void delete(User user);
    void deleteById(Long id);
    boolean existsByEmail(String email);
    long count();
    
    /**
     *Добавляем DTO методы (новые)
     */
    List<UserDTO> findAllDTO();
    Optional<UserDTO> findDTOById(Long id);
    Optional<UserDTO> findDTOByEmail(String email);
    List<UserDTO> findDTOByName(String name);
}