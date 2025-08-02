package se.campusmolndal.easyweather.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import se.campusmolndal.easyweather.models.WeatherInfo;
import se.campusmolndal.easyweather.service.AIArtisticWeatherService;

@Controller
public class AIArtisticWeatherController {

    private final WeatherAPIClient weatherAPIClient;
    private final AIArtisticWeatherService aiArtisticWeatherService;

    @Autowired
    public AIArtisticWeatherController(WeatherAPIClient weatherAPIClient, 
                                     AIArtisticWeatherService aiArtisticWeatherService) {
        this.weatherAPIClient = weatherAPIClient;
        this.aiArtisticWeatherService = aiArtisticWeatherService;
    }

    @GetMapping("/weather/ai-artistic")
    public ResponseEntity<String> getAIArtisticWeather(@RequestParam String city) {
        try {
            if (city == null || city.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("<p>City name is required</p>");
            }

            WeatherInfo weatherInfo = weatherAPIClient.fetchWeather(city.trim());
            if (weatherInfo != null) {
                String artisticComposition = aiArtisticWeatherService.generateArtisticCityImage(city, weatherInfo);
                return ResponseEntity.ok().body(artisticComposition);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("<p>Error generating artistic weather: " + e.getMessage() + "</p>");
        }
    }

    @GetMapping("/weather/ai-animation")
    public ResponseEntity<String> getAIWeatherAnimation(@RequestParam String city) {
        try {
            if (city == null || city.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("<p>City name is required</p>");
            }

            WeatherInfo weatherInfo = weatherAPIClient.fetchWeather(city.trim());
            if (weatherInfo != null) {
                String weatherAnimation = aiArtisticWeatherService.generateArtisticWeatherAnimation(city, weatherInfo);
                return ResponseEntity.ok().body(weatherAnimation);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("<p>Error generating weather animation: " + e.getMessage() + "</p>");
        }
    }

    @GetMapping("/weather/ai-cache-info")
    public ResponseEntity<String> getCacheInfo() {
        int cacheSize = aiArtisticWeatherService.getCacheSize();
        return ResponseEntity.ok().body(String.format("""
            <div style="font-family: Arial, sans-serif; padding: 20px; background: #f0f0f0; border-radius: 10px;">
                <h3>🤖 AI Artistic Weather Cache</h3>
                <p><strong>Cached Items:</strong> %d</p>
                <p><strong>Status:</strong> Active</p>
                <p><em>Cache helps avoid regenerating the same AI artwork</em></p>
                <button onclick="clearCache()" style="padding: 10px 20px; background: #ff6b6b; color: white; border: none; border-radius: 5px; cursor: pointer;">Clear Cache</button>
            </div>
            <script>
                function clearCache() {
                    fetch('/weather/ai-cache-clear', {method: 'POST'})
                        .then(() => location.reload());
                }
            </script>
            """, cacheSize));
    }

    @GetMapping("/weather/ai-cache-clear")
    public ResponseEntity<String> clearCache() {
        aiArtisticWeatherService.clearCache();
        return ResponseEntity.ok().body("Cache cleared successfully");
    }
}