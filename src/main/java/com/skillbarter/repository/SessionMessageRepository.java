package com.skillbarter.repository;

import com.skillbarter.entity.SessionMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionMessageRepository extends JpaRepository<SessionMessage, Long> {

    List<SessionMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
}
