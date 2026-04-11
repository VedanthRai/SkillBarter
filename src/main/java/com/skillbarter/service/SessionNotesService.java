package com.skillbarter.service;

import com.skillbarter.entity.Session;
import com.skillbarter.entity.SessionNotes;
import com.skillbarter.exception.BusinessRuleException;
import com.skillbarter.repository.SessionNotesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionNotesService {

    private final SessionNotesRepository sessionNotesRepository;
    private final SessionService sessionService;

    @Transactional
    public SessionNotes addOrUpdateNotes(Long sessionId, Long userId, String teacherNotes, 
                                         String learnerNotes, String keyTakeaways, 
                                         String homework, String resourcesShared) {
        Session session = sessionService.getSession(sessionId);

        boolean isTeacher = session.getTeacher().getId().equals(userId);
        boolean isLearner = session.getLearner().getId().equals(userId);

        if (!isTeacher && !isLearner) {
            throw new BusinessRuleException("You are not a participant in this session");
        }

        SessionNotes notes = sessionNotesRepository.findBySessionId(sessionId)
                .orElseGet(() -> SessionNotes.builder().session(session).build());

        if (isTeacher && teacherNotes != null) {
            notes.setTeacherNotes(teacherNotes);
        }
        if (isLearner && learnerNotes != null) {
            notes.setLearnerNotes(learnerNotes);
        }
        if (keyTakeaways != null) notes.setKeyTakeaways(keyTakeaways);
        if (homework != null) notes.setHomework(homework);
        if (resourcesShared != null) notes.setResourcesShared(resourcesShared);

        return sessionNotesRepository.save(notes);
    }

    public Optional<SessionNotes> getNotesForSession(Long sessionId) {
        return sessionNotesRepository.findBySessionId(sessionId);
    }

    @Transactional
    public SessionNotes addNotes(Long sessionId, Long userId, com.skillbarter.dto.SessionNotesDto dto) {
        return addOrUpdateNotes(sessionId, userId, dto.getTeacherNotes(), 
                               dto.getLearnerNotes(), dto.getKeyTakeaways(), 
                               dto.getHomework(), dto.getResourcesShared());
    }
}
