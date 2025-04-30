package com.fishtripplanner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainPageController {

    @GetMapping("/")
    public String rootRedirectToMain() {
        return "MainPage"; // templates/MainPage.html 로 이동
    }

    @GetMapping("/MainPage")
    public String showMainPage() {
        return "MainPage"; // templates/MainPage.html
    }

}
