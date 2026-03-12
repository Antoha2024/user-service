package org.example.userservice.dto;

/**
 * Data Transfer Object для пользователя.
 * Содержит только данные, необходимые для передачи между слоями приложения.
 * Технические поля (created_at, updated_at) скрыты.
 */
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private int age;
    
    /**
     * Получить ID пользователя.
     * @return идентификатор пользователя
     */
    public Long getId() {
        return id;
    }
    
    /**
     * Установить ID пользователя.
     * @param id идентификатор пользователя
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * Получить имя пользователя.
     * @return имя пользователя
     */
    public String getName() {
        return name;
    }
    
    /**
     * Установить имя пользователя.
     * @param name имя пользователя
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Получить email пользователя.
     * @return email пользователя
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * Установить email пользователя.
     * @param email email пользователя
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * Получить возраст пользователя.
     * @return возраст пользователя
     */
    public int getAge() {
        return age;
    }
    
    /**
     * Установить возраст пользователя.
     * @param age возраст пользователя
     */
    public void setAge(int age) {
        this.age = age;
    }
}