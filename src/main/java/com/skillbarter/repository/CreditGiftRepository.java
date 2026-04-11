package com.skillbarter.repository;

import com.skillbarter.entity.CreditGift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditGiftRepository extends JpaRepository<CreditGift, Long> {
    List<CreditGift> findBySenderIdOrRecipientIdOrderByCreatedAtDesc(Long senderId, Long recipientId);
}
