package se.campusmolndal.easyweather.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EnhancedWeatherController {

    @GetMapping("/weather/enhanced")
    public String enhancedWeather() {
        return "enhanced-weather.html";
    }

    @GetMapping("/weather/icons")
    public String weatherIcons() {
        return "enhanced-weather.html";
    }

    @GetMapping("/weather/test")
    public String testIntegration() {
        return "test-integration.html";
    }

    @GetMapping("/weather/handdrawn")
    public String handdrawnWeather() {
        return "handdrawn-weather.html";
    }

    @GetMapping("/weather/artistic")
    public String artisticWeather() {
        return "handdrawn-weather.html";
    }

    @GetMapping("/weather/ai-artistic-page")
    public String aiArtisticWeather() {
        return "ai-artistic-weather.html";
    }
}