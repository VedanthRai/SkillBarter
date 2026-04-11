package com.skillbarter.repository;

import com.skillbarter.entity.TeacherAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface TeacherAvailabilityRepository extends JpaRepository<TeacherAvailability, Long> {
    List<TeacherAvailability> findByTeacherIdAndIsActiveTrue(Long teacherId);
    List<TeacherAvailability> findByTeacherIdAndDayOfWeekAndIsActiveTrue(Long teacherId, DayOfWeek dayOfWeek);
}
