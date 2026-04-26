package com.studygrind.repository;

import com.studygrind.model.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    List<LoginHistory> findByUserIdOrderByLoginTimeDesc(Long userId);

    @Query("SELECT lh FROM LoginHistory lh WHERE lh.user.id = :userId ORDER BY lh.loginTime DESC")
    List<LoginHistory> findRecentLoginHistory(@Param("userId") Long userId);

    @Query("SELECT COUNT(lh) FROM LoginHistory lh WHERE lh.user.id = :userId AND lh.loginTime > :since")
    long countRecentLogins(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}