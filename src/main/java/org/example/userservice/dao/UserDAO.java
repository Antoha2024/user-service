package org.example.userservice.dao;

import org.example.userservice.entity.User;
import java.util.List;
import java.util.Optional;

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