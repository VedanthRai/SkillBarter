package com.skillbarter.service;

import com.skillbarter.entity.User;
import com.skillbarter.exception.ResourceNotFoundException;
import com.skillbarter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // In-memory token store (in production, use Redis or database)
    private final Map<String, TokenData> resetTokens = new HashMap<>();

    private static class TokenData {
        String email;
        LocalDateTime expiresAt;

        TokenData(String email, LocalDateTime expiresAt) {
            this.email = email;
            this.expiresAt = expiresAt;
        }
    }

    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        String token = UUID.randomUUID().toString();
        resetTokens.put(token, new TokenData(email, LocalDateTime.now().plusHours(1)));

        emailService.sendPasswordResetEmail(email, token);
        log.info("Password reset requested for {}", email);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        TokenData tokenData = resetTokens.get(token);
        
        if (tokenData == null) {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }

        if (LocalDateTime.now().isAfter(tokenData.expiresAt)) {
            resetTokens.remove(token);
            throw new IllegalArgumentException("Reset token has expired");
        }

        User user = userRepository.findByEmail(tokenData.email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetTokens.remove(token);
        log.info("Password reset successful for {}", tokenData.email);
    }

    public boolean isTokenValid(String token) {
        TokenData tokenData = resetTokens.get(token);
        return tokenData != null && LocalDateTime.now().isBefore(tokenData.expiresAt);
    }

    public void sendResetLink(String email) {
        requestPasswordReset(email);
    }
}
