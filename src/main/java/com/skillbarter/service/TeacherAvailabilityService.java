package com.skillbarter.service;

import com.skillbarter.entity.TeacherAvailability;
import com.skillbarter.entity.User;
import com.skillbarter.exception.ResourceNotFoundException;
import com.skillbarter.repository.TeacherAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherAvailabilityService {

    private final TeacherAvailabilityRepository availabilityRepository;
    private final UserService userService;

    @Transactional
    public TeacherAvailability addAvailability(Long teacherId, DayOfWeek dayOfWeek, 
                                               LocalTime startTime, LocalTime endTime) {
        User teacher = userService.findById(teacherId);

        TeacherAvailability availability = TeacherAvailability.builder()
                .teacher(teacher)
                .dayOfWeek(dayOfWeek)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        return availabilityRepository.save(availability);
    }

    @Transactional
    public void removeAvailability(Long availabilityId, Long teacherId) {
        TeacherAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found"));

        if (!availability.getTeacher().getId().equals(teacherId)) {
            throw new IllegalArgumentException("You can only remove your own availability");
        }

        availabilityRepository.delete(availability);
    }

    public List<TeacherAvailability> getAvailabilityForTeacher(Long teacherId) {
        return availabilityRepository.findByTeacherIdAndIsActiveTrue(teacherId);
    }

    public boolean isTeacherAvailable(Long teacherId, DayOfWeek dayOfWeek, LocalTime time) {
        List<TeacherAvailability> slots = availabilityRepository
                .findByTeacherIdAndDayOfWeekAndIsActiveTrue(teacherId, dayOfWeek);

        return slots.stream().anyMatch(slot -> 
                !time.isBefore(slot.getStartTime()) && !time.isAfter(slot.getEndTime()));
    }

    @Transactional
    public void deleteAvailability(Long availabilityId, Long teacherId) {
        removeAvailability(availabilityId, teacherId);
    }

    @Transactional
    public TeacherAvailability addAvailability(Long teacherId, com.skillbarter.dto.TeacherAvailabilityDto dto) {
        return addAvailability(teacherId, dto.getDayOfWeek(), dto.getStartTime(), dto.getEndTime());
    }
}
