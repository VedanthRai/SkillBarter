package com.skillbarter.repository;

import com.skillbarter.entity.User;
import com.skillbarter.enums.Role;
import com.skillbarter.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmailVerificationToken(String token);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<User> findByRole(Role role);

    List<User> findByStatus(UserStatus status);

    /** Leaderboard: top N users by reputation score */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' ORDER BY u.reputationScore DESC")
    List<User> findTopByReputationScore();

    /** Leaderboard: top N users by sessions completed */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' ORDER BY u.totalSessionsCompleted DESC")
    List<User> findTopBySessionsCompleted();

    /** Streak leaderboard */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' ORDER BY u.streakCount DESC")
    List<User> findTopByStreakCount();
}
