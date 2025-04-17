package com.fishtripplanner.controller.join;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class JoinController {

    @GetMapping("/join")
    public String joinChoice() {
        return "joinChoice"; // 위 html 경로
    }

    @GetMapping("/join/general")
    public String generalJoinForm() {
        return "joinGeneral"; // 일반회원 폼
    }

    @GetMapping("/join/business")
    public String businessJoinForm() {
        return "joinBusiness"; // 사업자회원 폼
    }
}