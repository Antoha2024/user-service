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
    private final KafkaEventProducerService kafkaProducer;
    
    @Autowired
    public UserServiceImpl(UserRepository userRepository, KafkaEventProducerService kafkaProducer) {
        this.userRepository = userRepository;
        this.kafkaProducer = kafkaProducer;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponseDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponseDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDTO);
    }
    
    @Override
    public UserResponseDTO createUser(UserRequestDTO userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new RuntimeException("User with email " + userRequest.getEmail() + " already exists");
        }
        
        User user = convertToEntity(userRequest);
        User savedUser = userRepository.save(user);
        UserResponseDTO response = convertToDTO(savedUser);
        
        kafkaProducer.sendUserEvent(UserEventDTO.OperationType.CREATE, savedUser.getEmail());
        
        return response;
    }

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
    
    @Override
    public boolean deleteUser(Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    String email = user.getEmail();
                    userRepository.deleteById(id);
                    kafkaProducer.sendUserEvent(UserEventDTO.OperationType.DELETE, email);
                    return true;
                })
                .orElse(false);
    }
    
    @Override
    @Transactional
    public int removeDuplicateUsers() {
        List<User> allUsers = userRepository.findAll();
        
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
        
        return totalDeleted;
    }
    
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
    
    private User convertToEntity(UserRequestDTO dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setAge(dto.getAge());
        return user;
    }
}