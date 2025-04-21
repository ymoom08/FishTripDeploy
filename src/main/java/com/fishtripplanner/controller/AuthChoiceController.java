package com.fishtripplanner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthChoiceController {

    @GetMapping("/chooseAuth")
    public String chooseAuthPage() {
        return "chooseAuth.html"; // templates/chooseAuth.html
    }
}
