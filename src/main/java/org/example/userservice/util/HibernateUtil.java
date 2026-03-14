package org.example.userservice.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Утилитный класс для работы с Hibernate.
 * Создает SessionFactory и предоставляет методы для работы с транзакциями.
 */
public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // Создаем конфигурацию из hibernate.cfg.xml
            Configuration configuration = new Configuration().configure();

            // Переопределяем настройки из System properties (если они есть)
            // Это позволяет Testcontainers подставить свой URL
            if (System.getProperty("hibernate.connection.url") != null) {
                configuration.setProperty("hibernate.connection.url",
                        System.getProperty("hibernate.connection.url"));
            }
            if (System.getProperty("hibernate.connection.username") != null) {
                configuration.setProperty("hibernate.connection.username",
                        System.getProperty("hibernate.connection.username"));
            }
            if (System.getProperty("hibernate.connection.password") != null) {
                configuration.setProperty("hibernate.connection.password",
                        System.getProperty("hibernate.connection.password"));
            }
            if (System.getProperty("hibernate.hbm2ddl.auto") != null) {
                configuration.setProperty("hibernate.hbm2ddl.auto",
                        System.getProperty("hibernate.hbm2ddl.auto"));
            }

            return configuration.buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    /**
     * Выполняет операцию внутри транзакции.
     */
    public static void doInTransaction(Consumer<Session> action) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            try {
                action.accept(session);
                session.getTransaction().commit();
            } catch (Exception e) {
                session.getTransaction().rollback();
                throw e;
            }
        }
    }

    /**
     * Выполняет операцию с возвратом результата внутри транзакции.
     */
    public static <R> R doInTransactionWithResult(Function<Session, R> action) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            try {
                R result = action.apply(session);
                session.getTransaction().commit();
                return result;
            } catch (Exception e) {
                session.getTransaction().rollback();
                throw e;
            }
        }
    }
}