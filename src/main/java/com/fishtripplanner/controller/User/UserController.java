package com.fishtripplanner.controller.User;

import com.fishtripplanner.api.user.UserService;
import com.fishtripplanner.domain.User;
import com.fishtripplanner.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 로그인 페이지
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // templates/login.html
    }

    // 내 정보 페이지
    @GetMapping("/profile")
    public String profilePage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        model.addAttribute("user", user);
        return "user/notification"; // templates/user/notification.html
    }

    // 내 정보 수정 폼
    @GetMapping("/profile/edit")
    public String editProfilePage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        model.addAttribute("user", user);
        return "user/profile_edit"; // templates/user/profile_edit.html
    }

    // 내 정보 수정 처리
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute("user") User updatedUser,
                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.updateUser(userDetails.getUser().getId(), updatedUser);
        return "redirect:/profile";
    }
}
