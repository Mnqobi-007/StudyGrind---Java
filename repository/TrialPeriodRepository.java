package com.studygrind.repository;

import com.studygrind.model.TrialPeriod;
import com.studygrind.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TrialPeriodRepository extends JpaRepository<TrialPeriod, Long> {
    Optional<TrialPeriod> findByStudentAndIsActiveTrue(User student);
    Optional<TrialPeriod> findTopByStudentOrderByStartDateDesc(User student);
    boolean existsByStudentAndIsActiveTrue(User student);
}