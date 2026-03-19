package com.userservice.aspect;

import com.userservice.dto.UserRequestDTO;
import com.userservice.repository.UserRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Aspect
@Component
public class DuplicateFilterAspect {
    
    private final UserRepository userRepository;
    
    @Autowired
    public DuplicateFilterAspect(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Pointcut("@annotation(com.userservice.annotation.FilterDuplicates)")
    public void filterDuplicatesPointcut() {}
    
    @Around("filterDuplicatesPointcut() && args(users,..)")
    public Object filterDuplicateUsers(ProceedingJoinPoint joinPoint, List<UserRequestDTO> users) throws Throwable {
        // Фильтрует дубликаты пользователей перед сохранением
        if (users == null || users.isEmpty()) {
            return joinPoint.proceed();
        }
        
        List<UserRequestDTO> uniqueUsers = users.stream()
                .filter(user -> !userRepository.existsByEmail(user.getEmail()))
                .collect(Collectors.toList());
        
        Object[] args = Arrays.stream(joinPoint.getArgs())
                .map(arg -> arg instanceof List ? uniqueUsers : arg)
                .toArray();
        
        return joinPoint.proceed(args);
    }
}