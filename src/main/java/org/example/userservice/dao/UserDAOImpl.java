package org.example.userservice.dao;

import org.example.userservice.dto.UserDTO;
import org.example.userservice.entity.User;
import org.example.userservice.util.HibernateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

/**
 * Реализация интерфейса UserDAO с использованием Hibernate ORM.
 * Обеспечивает все CRUD операции с базой данных через Hibernate Session.
 * Каждый метод открывает новую сессию и управляет транзакциями.
 */
public class UserDAOImpl implements UserDAO {
    private static final Logger logger = LogManager.getLogger(UserDAOImpl.class);

    /** 
     * Методы CRUD
     */ 
    @Override
    public User save(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(user);
            transaction.commit();
            logger.info("User saved successfully: {}", user.getEmail());
            return user;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Failed to save user: {}", e.getMessage());
            throw new RuntimeException("Failed to save user", e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            logger.debug("Found user by id {}: {}", id, user != null ? user.getEmail() : "not found");
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("Failed to find user by id {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User WHERE email = :email", User.class);
            query.setParameter("email", email);
            User user = query.getSingleResult();
            logger.debug("Found user by email {}: {}", email, user != null);
            return Optional.ofNullable(user);
        } catch (NoResultException e) {
            logger.debug("No user found with email: {}", email);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Failed to find user by email {}: {}", email, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<User> query = builder.createQuery(User.class);
            Root<User> root = query.from(User.class);
            query.select(root);

            List<User> users = session.createQuery(query).getResultList();
            logger.debug("Found {} users", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Failed to find all users: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<User> findByName(String name) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User WHERE name LIKE :name", User.class);
            query.setParameter("name", "%" + name + "%");
            List<User> users = query.getResultList();
            logger.debug("Found {} users with name containing '{}'", users.size(), name);
            return users;
        } catch (Exception e) {
            logger.error("Failed to find users by name {}: {}", name, e.getMessage());
            return List.of();
        }
    }

    @Override
    public User update(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.update(user);
            transaction.commit();
            logger.info("User updated successfully: {}", user.getEmail());
            return user;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Failed to update user: {}", e.getMessage());
            throw new RuntimeException("Failed to update user", e);
        }
    }

    @Override
    public void delete(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.delete(user);
            transaction.commit();
            logger.info("User deleted successfully: {}", user.getEmail());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Failed to delete user: {}", e.getMessage());
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        findById(id).ifPresent(this::delete);
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(id) FROM User", Long.class);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.error("Failed to count users: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Новые методы DTO через интерфейс EntityManager (hibernate.Session)
     */  
    @Override
    public List<UserDTO> findAllDTO() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<UserDTO> users = session.createQuery(
                "SELECT new org.example.userservice.dto.UserDTO(u.name, u.email, u.age) FROM User u", 
                UserDTO.class)
                .getResultList();
            logger.debug("Found {} users via DTO projection", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Failed to find all users via DTO: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public Optional<UserDTO> findDTOById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            UserDTO user = session.createQuery(
                "SELECT new org.example.userservice.dto.UserDTO(u.name, u.email, u.age) FROM User u WHERE u.id = :id", 
                UserDTO.class)
                .setParameter("id", id)
                .uniqueResultOptional()
                .orElse(null);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("Failed to find user DTO by id {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<UserDTO> findDTOByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            UserDTO user = session.createQuery(
                "SELECT new org.example.userservice.dto.UserDTO(u.name, u.email, u.age) FROM User u WHERE u.email = :email", 
                UserDTO.class)
                .setParameter("email", email)
                .uniqueResultOptional()
                .orElse(null);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("Failed to find user DTO by email {}: {}", email, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<UserDTO> findDTOByName(String name) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<UserDTO> users = session.createQuery(
                "SELECT new org.example.userservice.dto.UserDTO(u.name, u.email, u.age) FROM User u WHERE u.name LIKE :name", 
                UserDTO.class)
                .setParameter("name", "%" + name + "%")
                .getResultList();
            logger.debug("Found {} users DTO with name containing '{}'", users.size(), name);
            return users;
        } catch (Exception e) {
            logger.error("Failed to find users DTO by name {}: {}", name, e.getMessage());
            return List.of();
        }
    }
}