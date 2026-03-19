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
    
    @Pointcut("execution(* com.userservice.service.UserService.createUser(..)) && args(userRequest)")
    public void createUserPointcut(UserRequestDTO userRequest) {}
    
    @Around(value = "createUserPointcut(userRequest)", argNames = "joinPoint,userRequest")
    public Object filterDuplicateUser(ProceedingJoinPoint joinPoint, UserRequestDTO userRequest) throws Throwable {
        // Проверяем, существует ли пользователь с таким email
        if (userRequest != null && userRepository.existsByEmail(userRequest.getEmail())) {
            throw new RuntimeException("User with email " + userRequest.getEmail() + " already exists");
        }
        
        return joinPoint.proceed();
    }
}