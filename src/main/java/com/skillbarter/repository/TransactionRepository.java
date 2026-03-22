package com.skillbarter.repository;

import com.skillbarter.entity.Transaction;
import com.skillbarter.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findBySessionId(Long sessionId);

    List<Transaction> findByPayerId(Long payerId);

    List<Transaction> findByPayeeId(Long payeeId);

    List<Transaction> findByStatus(TransactionStatus status);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.payee.id = :userId AND t.status = 'RELEASED'")
    BigDecimal sumReleasedEarningsForUser(@Param("userId") Long userId);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.payer.id = :userId AND t.status = 'RELEASED'")
    BigDecimal sumSpentCreditsForUser(@Param("userId") Long userId);

    @Query("SELECT t FROM Transaction t WHERE (t.payer.id = :userId OR t.payee.id = :userId) ORDER BY t.createdAt DESC")
    List<Transaction> findAllByUserId(@Param("userId") Long userId);
}
