package com.skillbarter.repository;

import com.skillbarter.entity.SessionNotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionNotesRepository extends JpaRepository<SessionNotes, Long> {
    Optional<SessionNotes> findBySessionId(Long sessionId);
}
