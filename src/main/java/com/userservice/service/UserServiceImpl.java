package com.userservice.service;

import com.userservice.dto.UserEventDTO;
import com.userservice.dto.UserRequestDTO;
import com.userservice.dto.UserResponseDTO;
import com.userservice.entity.User;
import com.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
<<<<<<< HEAD
    private final KafkaEventProducerService kafkaProducer;
    
=======

>>>>>>> 30bd309866d4e8236e0ded37bcd424e6d9ac92a7
    @Autowired
    public UserServiceImpl(UserRepository userRepository, KafkaEventProducerService kafkaProducer) {
        this.userRepository = userRepository;
        this.kafkaProducer = kafkaProducer;
    }

    /**
     * Получение всех пользователей (транзакция чтения)
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Поиск пользователя по ID (транзакция чтения)
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponseDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO);
    }

    /**
     * Поиск пользователя по email (транзакция чтения)
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponseDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDTO);
    }
<<<<<<< HEAD
    
=======

>>>>>>> 30bd309866d4e8236e0ded37bcd424e6d9ac92a7
    /**
     * Создание нового пользователя с проверкой на дубликат email
     * После успешного создания отправляет событие в Kafka
     */
    @Override
    public UserResponseDTO createUser(UserRequestDTO userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new RuntimeException("User with email " + userRequest.getEmail() + " already exists");
        }

        User user = convertToEntity(userRequest);
        User savedUser = userRepository.save(user);
        UserResponseDTO response = convertToDTO(savedUser);
        
        // Отправка события в Kafka о создании пользователя
        kafkaProducer.sendUserEvent(UserEventDTO.OperationType.CREATE, savedUser.getEmail());
        
        return response;
    }

    /**
     * Обновление пользователя с проверкой email
     */
    @Override
    public Optional<UserResponseDTO> updateUser(Long id, UserRequestDTO userRequest) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    if (!existingUser.getEmail().equals(userRequest.getEmail()) &&
                            userRepository.existsByEmail(userRequest.getEmail())) {
                        throw new RuntimeException("Email " + userRequest.getEmail() + " is already taken");
                    }

                    existingUser.setEmail(userRequest.getEmail());
                    existingUser.setFirstName(userRequest.getFirstName());
                    existingUser.setLastName(userRequest.getLastName());
                    existingUser.setAge(userRequest.getAge());

                    User updatedUser = userRepository.save(existingUser);
                    return convertToDTO(updatedUser);
                });
    }

    /**
     * Удаление пользователя
     * После успешного удаления отправляет событие в Kafka
     */
    @Override
    public boolean deleteUser(Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    String email = user.getEmail();
                    userRepository.deleteById(id);
                    // Отправка события в Kafka об удалении пользователя
                    kafkaProducer.sendUserEvent(UserEventDTO.OperationType.DELETE, email);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Поиск и удаление дубликатов по email.
<<<<<<< HEAD
     * 
=======
     *
>>>>>>> 30bd309866d4e8236e0ded37bcd424e6d9ac92a7
     * Алгоритм работы:
     * 1. Загружаем всех пользователей из БД
     * 2. Группируем их по email для выявления дубликатов
     * 3. Для каждой группы дубликатов (где количество > 1):
     *    - Определяем список ID "лишних" пользователей (пропускаем первого)
     *    - Удаляем их с проверкой: DELETE WHERE email = :email AND id IN (:ids)
<<<<<<< HEAD
     * 
     * Такой подход гарантирует, что даже если в списке ID окажется посторонний ID
     * (например, из-за бага в логике формирования списка), он не будет удалён,
     * так как не соответствует указанному email группы дубликатов.
     * 
=======
     *
     * Такой подход гарантирует, что даже если в списке ID окажется посторонний ID
     * (например, из-за бага в логике формирования списка), он не будет удалён,
     * так как не соответствует указанному email группы дубликатов.
     *
>>>>>>> 30bd309866d4e8236e0ded37bcd424e6d9ac92a7
     * @return количество удалённых пользователей-дубликатов
     */
    @Override
    @Transactional
    public int removeDuplicateUsers() {
        // Загружаем всех пользователей
        List<User> allUsers = userRepository.findAll();
<<<<<<< HEAD
        
        Map<String, List<User>> usersByEmail = allUsers.stream()
                .collect(Collectors.groupingBy(User::getEmail));
        
        int totalDeleted = 0;
        
        for (Map.Entry<String, List<User>> entry : usersByEmail.entrySet()) {
            List<User> users = entry.getValue();
            
            if (users.size() > 1) {
                List<Long> idsToDelete = users.stream()
                        .skip(1)
                        .map(User::getId)
                        .collect(Collectors.toList());
                
                totalDeleted += userRepository.deleteDuplicatesByEmail(
                        entry.getKey(),
                        idsToDelete
                );
            }
        }
        
=======

        // Группируем по email
        Map<String, List<User>> usersByEmail = allUsers.stream()
                .collect(Collectors.groupingBy(User::getEmail));

        int totalDeleted = 0;

        // Обрабатываем каждую группу пользователей с одинаковым email
        for (Map.Entry<String, List<User>> entry : usersByEmail.entrySet()) {
            List<User> users = entry.getValue();

            // Если в группе больше одного пользователя — есть дубликаты
            if (users.size() > 1) {
                // Формируем список ID для удаления (все, кроме первого)
                List<Long> idsToDelete = users.stream()
                        .skip(1)                    // Пропускаем первого пользователя в группе
                        .map(User::getId)           // Берём ID остальных
                        .collect(Collectors.toList());

                // Удаляем с проверкой email — безопасное удаление
                totalDeleted += userRepository.deleteDuplicatesByEmail(
                        entry.getKey(),             // email группы
                        idsToDelete                 // список ID для удаления
                );
            }
        }

>>>>>>> 30bd309866d4e8236e0ded37bcd424e6d9ac92a7
        return totalDeleted;
    }

    /**
     * Конвертер Entity -> DTO
     */
    private UserResponseDTO convertToDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setAge(user.getAge());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    /**
     * Конвертер DTO -> Entity
     */
    private User convertToEntity(UserRequestDTO dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setAge(dto.getAge());
        return user;
    }
}