package org.example.userservice.dao;

import org.example.userservice.entity.User;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс Data Access Object (DAO) для управления пользователями.
 * Определяет контракт для операций с базой данных, следуя паттерну Repository.
 * Все методы должны быть реализованы конкретным DAO классом.
 */ 
public interface UserDAO {
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
}