package com.fishtripplanner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainPageController {

    @GetMapping("/MainPage")
    public String showMainPage() {
        return "MainPage"; // templates/MainPage.html 을 반환
    }

}
