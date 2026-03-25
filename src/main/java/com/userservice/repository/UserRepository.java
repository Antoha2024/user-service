package com.userservice.repository;

import com.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Поиск пользователя по email
     */
    Optional<User> findByEmail(String email);

    /**
     * Проверка существования email
     */
    boolean existsByEmail(String email);

    /**
     * Поиск по фамилии
     */
    List<User> findByLastName(String lastName);

    /**
     * Поиск по возрастному диапазону
     */
    @Query("SELECT u FROM User u WHERE u.age BETWEEN :minAge AND :maxAge")
    List<User> findByAgeRange(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);

    /**
     * Удаление дубликатов по email с проверкой
     * Удаляет только тех пользователей, у которых email соответствует указанному,
     * а ID входит в переданный список. Это обеспечивает безопасность удаления —
     * даже если в списке ID окажется посторонний ID, он не будет удалён,
     * так как не соответствует указанному email.
<<<<<<< HEAD
=======
     *
     * @param email email пользователя (группа дубликатов)
     * @param ids список ID, которые нужно удалить из этой группы
     * @return количество удалённых записей
>>>>>>> 30bd309866d4e8236e0ded37bcd424e6d9ac92a7
     */
    
    @Transactional
    @Modifying
    @Query("DELETE FROM User u WHERE u.email = :email AND u.id IN :ids")
    int deleteDuplicatesByEmail(@Param("email") String email, @Param("ids") List<Long> ids);
}