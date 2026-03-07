package org.example.userservice.dto;

public class UserDTO {
    private String name;
    private String email;
    private Integer age;

    public UserDTO() {}

    public UserDTO(String name, String email, Integer age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }
    /* Геттеры и сеттеры  */
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Integer getAge() { return age; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setAge(Integer age) { this.age = age; }

    @Override
    public String toString() {
        return String.format("Пользователь{имя='%s', email='%s', возраст=%d}", name, email, age);
    }
}