package com.skillbarter.repository;

import com.skillbarter.entity.Dispute;
import com.skillbarter.enums.DisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {

    List<Dispute> findByStatus(DisputeStatus status);

    List<Dispute> findByRaisedById(Long userId);

    List<Dispute> findByAssignedVerifierId(Long verifierId);

    Optional<Dispute> findBySessionId(Long sessionId);

    boolean existsBySessionId(Long sessionId);
}
