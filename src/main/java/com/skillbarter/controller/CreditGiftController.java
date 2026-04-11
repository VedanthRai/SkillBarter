package com.skillbarter.controller;

import com.skillbarter.dto.CreditGiftDto;
import com.skillbarter.entity.User;
import com.skillbarter.service.CreditGiftService;
import com.skillbarter.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/gifts")
@RequiredArgsConstructor
public class CreditGiftController {

    private final CreditGiftService creditGiftService;
    private final UserService userService;

    @GetMapping("/send")
    public String sendGiftForm(@RequestParam(required = false) Long recipientId, Model model) {
        model.addAttribute("giftDto", new CreditGiftDto());
        if (recipientId != null) {
            var recipient = userService.findById(recipientId);
            model.addAttribute("recipient", recipient);
        }
        return "gifts/send";
    }

    @PostMapping("/send")
    public String sendGift(@Valid @ModelAttribute CreditGiftDto dto,
                          @AuthenticationPrincipal User user,
                          RedirectAttributes ra) {
        creditGiftService.sendGift(user.getId(), dto);
        ra.addFlashAttribute("success", "Gift sent successfully!");
        return "redirect:/wallet";
    }

    @GetMapping("/history")
    public String giftHistory(@AuthenticationPrincipal User user, Model model) {
        var gifts = creditGiftService.getGiftHistory(user.getId());
        model.addAttribute("gifts", gifts);
        return "gifts/history";
    }
}
