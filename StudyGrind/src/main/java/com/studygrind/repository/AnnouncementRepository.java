package com.studygrind.repository;

import com.studygrind.model.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    
    List<Announcement> findByCourseIdOrderByCreatedAtDesc(Long courseId);
    
    @Query("SELECT a FROM Announcement a WHERE a.course.id IN " +
           "(SELECT ce.course.id FROM CourseEnrollment ce WHERE ce.student.id = :studentId) " +
           "ORDER BY a.createdAt DESC")
    List<Announcement> findAnnouncementsForStudent(@Param("studentId") Long studentId);
    
    @Query("SELECT a FROM Announcement a WHERE a.course.teacher.id = :teacherId ORDER BY a.createdAt DESC")
    List<Announcement> findAnnouncementsByTeacherId(@Param("teacherId") Long teacherId);
}