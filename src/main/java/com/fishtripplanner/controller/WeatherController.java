package com.fishtripplanner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WeatherController {

    @GetMapping("/weather")
    public String showWeatherPage() {
        return "weather";  // templates/weather.html 로 연결
    }
}
