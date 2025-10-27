package app.repository;

import app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    List<User> findByNameContainingIgnoreCase(String name);

    List<User> findByAgeBetween(int minAge, int maxAge);

    @Modifying
    @Query("UPDATE User u SET u.age = :age WHERE u.id = :id")
    void updateUserAge(@Param("id") int id, @Param("age") int age);

    @Query("SELECT u FROM User u WHERE u.name LIKE %:name% AND u.email LIKE %:email%")
    List<User> findByNameAndEmail(@Param("name") String name, @Param("email") String email);
}