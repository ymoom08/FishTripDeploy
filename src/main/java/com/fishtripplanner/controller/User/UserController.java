package com.fishtripplanner.controller.User;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @GetMapping("/notifications")
    public String userNotifications(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");

        if (sessionUser == null) {
            return "redirect:/login";
        }

        User user = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        model.addAttribute("user", user);
        return "notifications";
    }
}
