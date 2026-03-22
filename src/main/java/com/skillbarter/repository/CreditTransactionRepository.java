package com.skillbarter.repository;

import com.skillbarter.entity.CreditTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, Long> {

    List<CreditTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT COALESCE(SUM(ct.amount), 0) FROM CreditTransaction ct WHERE ct.user.id = :userId AND ct.amount > 0")
    BigDecimal sumCreditsEarned(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(ct.amount), 0) FROM CreditTransaction ct WHERE ct.user.id = :userId AND ct.amount < 0")
    BigDecimal sumCreditsSpent(@Param("userId") Long userId);

    @Query("SELECT COUNT(ct) FROM CreditTransaction ct WHERE ct.type = 'ADMIN_GRANT'")
    long countAdminGrants();

    @Query("SELECT COALESCE(SUM(ct.amount), 0) FROM CreditTransaction ct WHERE ct.type = 'ADMIN_GRANT'")
    BigDecimal sumAdminGrants();
}
