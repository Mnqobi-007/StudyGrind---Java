package com.studygrind.repository;

import com.studygrind.model.Timetable;
import com.studygrind.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TimetableRepository extends JpaRepository<Timetable, Long> {
    List<Timetable> findByStudent(User student);

    @Query("SELECT t FROM Timetable t WHERE t.student.id = :userId AND t.dayOfWeek = :dayOfWeek ORDER BY t.startTime")
    List<Timetable> findByUserIdAndDayOfWeek(@Param("userId") Long userId, @Param("dayOfWeek") String dayOfWeek);

    @Query("SELECT t FROM Timetable t WHERE t.student.id = :studentId ORDER BY FIELD(t.dayOfWeek, 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'), t.startTime")
    List<Timetable> findOrderedByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT t FROM Timetable t WHERE t.student.id = :studentId AND t.dayOfWeek = :dayOfWeek ORDER BY t.startTime")
    List<Timetable> findByStudentIdAndDayOfWeek(@Param("studentId") Long studentId, @Param("dayOfWeek") String dayOfWeek);

    void deleteByStudentAndId(User student, Long id);

    @Query("SELECT t FROM Timetable t WHERE t.student.role = :role ORDER BY t.dayOfWeek, t.startTime")
    List<Timetable> findByStudentRole(@Param("role") String role);
}