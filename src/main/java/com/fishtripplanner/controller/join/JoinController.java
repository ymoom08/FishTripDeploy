package com.fishtripplanner.controller.join;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.dto.user.JoinRequest;
import com.fishtripplanner.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class JoinController {

    @GetMapping("/join")
    public String joinChoice() {
        return "joinChoice"; // 위 html 경로
    }

    @GetMapping("/join/normal")
    public String generalJoinForm() {
        return "joinNormal"; // 일반회원 폼
    }

    @GetMapping("/join/business")
    public String businessJoinForm() {
        return "joinBusiness"; // 사업자회원 폼
    }

}