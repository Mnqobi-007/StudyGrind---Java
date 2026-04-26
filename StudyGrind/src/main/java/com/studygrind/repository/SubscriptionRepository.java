package com.studygrind.repository;

import com.studygrind.model.Subscription;
import com.studygrind.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByStudent(User student);
    Optional<Subscription> findTopByStudentAndStatusOrderByEndDateDesc(User student, String status);
    Optional<Subscription> findTopByStudentOrderByEndDateDesc(User student);
    Optional<Subscription> findByStripePaymentId(String paymentId);
    
    @Query("SELECT s FROM Subscription s WHERE s.status = 'active' AND s.endDate < CURRENT_TIMESTAMP")
    List<Subscription> findExpiredActiveSubscriptions();
    
    boolean existsByStudentAndStatus(User student, String status);
    
    @Query("SELECT SUM(s.amount) FROM Subscription s WHERE s.status = 'active'")
    Double getTotalRevenue();
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status = 'active'")
    Long getActiveSubscriptionsCount();
    
    @Query("SELECT SUM(s.amount) FROM Subscription s WHERE s.status = 'active' AND MONTH(s.paymentDate) = MONTH(CURRENT_DATE) AND YEAR(s.paymentDate) = YEAR(CURRENT_DATE)")
    Double getMonthlyRevenue();
    
    @Query("SELECT s FROM Subscription s WHERE s.status = 'active' ORDER BY s.paymentDate DESC")
    List<Subscription> findRecentPayments(Pageable pageable);
}