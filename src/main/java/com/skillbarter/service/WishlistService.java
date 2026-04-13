package com.skillbarter.service;

import com.skillbarter.entity.Skill;
import com.skillbarter.entity.User;
import com.skillbarter.entity.WishlistItem;
import com.skillbarter.exception.BusinessRuleException;
import com.skillbarter.repository.SkillRepository;
import com.skillbarter.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistService {

    private final WishlistRepository wishlistRepo;
    private final SkillRepository skillRepository;
    private final UserService userService;

    @Transactional
    public WishlistItem addToWishlist(Long userId, Long skillId, boolean alertEnabled) {
        if (wishlistRepo.existsByUserIdAndSkillId(userId, skillId)) {
            throw new BusinessRuleException("Skill already in your wishlist.");
        }
        User user = userService.findById(userId);
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new BusinessRuleException("Skill not found: " + skillId));

        if (skill.getUser().getId().equals(userId)) {
            throw new BusinessRuleException("You cannot wishlist your own skill.");
        }

        WishlistItem item = WishlistItem.builder()
                .user(user)
                .skill(skill)
                .alertEnabled(alertEnabled)
                .build();

        log.info("User {} added skill {} to wishlist", userId, skillId);
        return wishlistRepo.save(item);
    }

    @Transactional
    public void removeFromWishlist(Long userId, Long skillId) {
        if (!wishlistRepo.existsByUserIdAndSkillId(userId, skillId)) {
            throw new BusinessRuleException("Skill not in your wishlist.");
        }
        wishlistRepo.deleteByUserIdAndSkillId(userId, skillId);
    }

    public List<WishlistItem> getWishlistForUser(Long userId) {
        return wishlistRepo.findByUserId(userId);
    }

    public boolean isWishlisted(Long userId, Long skillId) {
        return wishlistRepo.existsByUserIdAndSkillId(userId, skillId);
    }

    /** Returns users who want to be alerted about this skill */
    public List<WishlistItem> getAlertSubscribers(Long skillId) {
        return wishlistRepo.findBySkillIdAndAlertEnabledTrue(skillId);
    }
}
