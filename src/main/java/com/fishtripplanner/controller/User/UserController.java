package com.fishtripplanner.controller.User;

import com.fishtripplanner.dto.user.JoinRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/login")
    public String loginForm() {
        return "login"; // 로그인 폼 (Spring Security 자동 처리)
    }


}
