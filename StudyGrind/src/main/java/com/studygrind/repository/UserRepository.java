package com.studygrind.repository;

import com.studygrind.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByStudentNumber(String studentNumber);
    List<User> findByRole(String role);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByStudentNumber(String studentNumber);

    @Query("SELECT u FROM User u WHERE u.role = :role ORDER BY u.createdAt DESC")
    List<User> findUsersByRole(@Param("role") String role);

    // Verification queries
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.verificationStatus = :status ORDER BY u.createdAt ASC")
    List<User> findByRoleAndVerificationStatus(@Param("role") String role, @Param("status") String status);

    @Query("SELECT COUNT(u) FROM User u WHERE u.verificationStatus = 'pending' AND u.role = 'student'")
    long countPendingVerifications();
}