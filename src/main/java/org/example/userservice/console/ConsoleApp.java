package org.example.userservice.console;

import org.example.userservice.dao.UserDAO;
import org.example.userservice.dao.UserDAOImpl;
import org.example.userservice.dto.UserDTO;
import org.example.userservice.entity.User;
import org.example.userservice.util.HibernateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Консольный интерфейс для управления пользователями.
 * Предоставляет интерактивное меню для выполнения CRUD операций
 * с пользователями через командную строку.
 * 
 * Класс использует паттерн "Фасад" для работы с DAO слоем
 * и обеспечивает пользовательский ввод через Scanner.
 */
public class ConsoleApp {
    private static final Logger logger = LogManager.getLogger(ConsoleApp.class);
    private final UserDAO userDAO;
    private final Scanner scanner;

    public ConsoleApp() {
        this.userDAO = new UserDAOImpl();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        logger.info("Starting User Service Console Application");
        printWelcomeMessage();

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readIntInput("Выберите опцию: ");

            try {
                switch (choice) {
                    case 1:
                        createUser();
                        break;
                    case 2:
                        findUserById();
                        break;
                    case 3:
                        findUserByEmail();
                        break;
                    case 4:
                        findAllUsers();
                        break;
                    case 5:
                        findUsersByName();
                        break;
                    case 6:
                        updateUser();
                        break;
                    case 7:
                        deleteUser();
                        break;
                    case 8:
                        showStatistics();
                        break;
                    case 0:
                        running = false;
                        System.out.println("Завершение работы...");
                        break;
                    default:
                        System.out.println("Неверная опция. Попробуйте снова.");
                }
            } catch (Exception e) {
                logger.error("Error during operation: {}", e.getMessage());
                System.out.println("Произошла ошибка: " + e.getMessage());
            }

            if (running && choice != 0) {
                System.out.println("\nНажмите Enter для продолжения...");
                scanner.nextLine();
            }
        }

        scanner.close();
        HibernateUtil.shutdown();
    }

    private void printWelcomeMessage() {
        System.out.println("=================================");
        System.out.println("  User Service Console Application");
        System.out.println("=================================");
    }

    private void printMenu() {
        System.out.println("\n--- Меню ---");
        System.out.println("1. Создать нового пользователя");
        System.out.println("2. Найти пользователя по ID");
        System.out.println("3. Найти пользователя по email");
        System.out.println("4. Показать всех пользователей");
        System.out.println("5. Найти пользователей по имени");
        System.out.println("6. Обновить данные пользователя");
        System.out.println("7. Удалить пользователя");
        System.out.println("8. Статистика");
        System.out.println("0. Выход");
    }

    private void createUser() {
        System.out.println("\n--- Создание нового пользователя ---");

        String name = readStringInput("Введите имя: ");
        String email = readStringInput("Введите email: ");

        if (userDAO.existsByEmail(email)) {
            System.out.println("Пользователь с таким email уже существует!");
            return;
        }

        int age = readIntInput("Введите возраст: ");

        User user = new User(name, email, age);
        userDAO.save(user);
        
        /** 
         * Показываем созданного пользователя через DTO
         */ 
        userDAO.findDTOByEmail(email).ifPresent(dto -> {
            System.out.println("Пользователь успешно создан!");
            System.out.println(dto);
        });
    }

    private void findUserById() {
        System.out.println("\n--- Поиск пользователя по ID ---");
        Long id = readLongInput("Введите ID пользователя: ");

        Optional<UserDTO> userOpt = userDAO.findDTOById(id);
        if (userOpt.isPresent()) {
            System.out.println("Найден пользователь:");
            System.out.println(userOpt.get());
        } else {
            System.out.println("Пользователь с ID " + id + " не найден.");
        }
    }

    private void findUserByEmail() {
        System.out.println("\n--- Поиск пользователя по email ---");
        String email = readStringInput("Введите email: ");

        Optional<UserDTO> userOpt = userDAO.findDTOByEmail(email);
        if (userOpt.isPresent()) {
            System.out.println("Найден пользователь:");
            System.out.println(userOpt.get());
        } else {
            System.out.println("Пользователь с email " + email + " не найден.");
        }
    }

    private void findAllUsers() {
        System.out.println("\n--- Все пользователи ---");
        List<UserDTO> users = userDAO.findAllDTO();

        if (users.isEmpty()) {
            System.out.println("Пользователи не найдены.");
        } else {
            System.out.println("Найдено пользователей: " + users.size());
            for (int i = 0; i < users.size(); i++) {
                System.out.println((i + 1) + ". " + users.get(i));
            }
        }
    }

    private void findUsersByName() {
        System.out.println("\n--- Поиск пользователей по имени ---");
        String name = readStringInput("Введите имя (или часть имени): ");

        List<UserDTO> users = userDAO.findDTOByName(name);

        if (users.isEmpty()) {
            System.out.println("Пользователи с именем '" + name + "' не найдены.");
        } else {
            System.out.println("Найдено пользователей: " + users.size());
            for (int i = 0; i < users.size(); i++) {
                System.out.println((i + 1) + ". " + users.get(i));
            }
        }
    }

    private void updateUser() {
        System.out.println("\n--- Обновление данных пользователя ---");
        Long id = readLongInput("Введите ID пользователя для обновления: ");

        Optional<User> userOpt = userDAO.findById(id);
        if (userOpt.isEmpty()) {
            System.out.println("Пользователь с ID " + id + " не найден.");
            return;
        }

        User user = userOpt.get();
        /**
         * Показываем текущие данные через DTO
         */ 
        userDAO.findDTOById(id).ifPresent(dto -> {
            System.out.println("Текущие данные: " + dto);
        });

        System.out.println("Введите новые данные (оставьте пустым для сохранения текущего значения):");

        String name = readStringInputWithDefault("Новое имя", user.getName());
        if (!name.isEmpty()) {
            user.setName(name);
        }

        String email = readStringInputWithDefault("Новый email", user.getEmail());
        if (!email.isEmpty() && !email.equals(user.getEmail())) {
            if (userDAO.existsByEmail(email)) {
                System.out.println("Email " + email + " уже используется другим пользователем!");
            } else {
                user.setEmail(email);
            }
        }

        String ageStr = readStringInputWithDefault("Новый возраст", String.valueOf(user.getAge()));
        if (!ageStr.isEmpty()) {
            try {
                user.setAge(Integer.parseInt(ageStr));
            } catch (NumberFormatException e) {
                System.out.println("Неверный формат возраста. Возраст не изменен.");
            }
        }

        userDAO.update(user);
        System.out.println("Данные пользователя обновлены!");
        /**
         * Показываем обновленные данные через DTO
         */ 
        userDAO.findDTOById(id).ifPresent(dto -> {
            System.out.println("Новые данные: " + dto);
        });
    }

    private void deleteUser() {
        System.out.println("\n--- Удаление пользователя ---");
        Long id = readLongInput("Введите ID пользователя для удаления: ");

        Optional<User> userOpt = userDAO.findById(id);
        if (userOpt.isEmpty()) {
            System.out.println("Пользователь с ID " + id + " не найден.");
            return;
        }

        User user = userOpt.get();
        /** 
         * Показываем данные пользователя через DTO перед удалением
         */ 
        userDAO.findDTOById(id).ifPresent(dto -> {
            System.out.println("Вы собираетесь удалить пользователя:");
            System.out.println(dto);
        });

        String confirm = readStringInput("Подтвердите удаление (yes/no): ");
        if (confirm.equalsIgnoreCase("yes") || confirm.equalsIgnoreCase("y")) {
            userDAO.delete(user);
            System.out.println("Пользователь успешно удален!");
        } else {
            System.out.println("Удаление отменено.");
        }
    }

    private void showStatistics() {
        System.out.println("\n--- Статистика ---");
        long totalUsers = userDAO.count();
        System.out.println("Всего пользователей в системе: " + totalUsers);

        List<UserDTO> users = userDAO.findAllDTO();
        if (!users.isEmpty()) {
            double avgAge = users.stream()
                    .mapToInt(UserDTO::getAge)
                    .average()
                    .orElse(0);
            System.out.printf("Средний возраст: %.1f%n", avgAge);
        }
    }
    
    /** 
     * Вспомогательные методы ввода
     */ 
    private String readStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private String readStringInputWithDefault(String prompt, String defaultValue) {
        System.out.print(prompt + " [" + defaultValue + "]: ");
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? defaultValue : input;
    }

    private int readIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Пожалуйста, введите корректное число.");
            }
        }
    }

    private Long readLongInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Long.parseLong(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Пожалуйста, введите корректное число.");
            }
        }
    }
}