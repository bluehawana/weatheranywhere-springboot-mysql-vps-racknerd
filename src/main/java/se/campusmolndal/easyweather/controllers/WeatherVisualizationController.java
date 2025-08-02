package se.campusmolndal.easyweather.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.campusmolndal.easyweather.models.WeatherInfo;
import se.campusmolndal.easyweather.service.WeatherVisualizationService;

@RestController
public class WeatherVisualizationController {

    private final WeatherAPIClient weatherAPIClient;
    private final WeatherVisualizationService visualizationService;
    private static final Logger log = LoggerFactory.getLogger(WeatherVisualizationController.class);

    @Autowired
    public WeatherVisualizationController(WeatherAPIClient weatherAPIClient, 
                                        WeatherVisualizationService visualizationService) {
        this.weatherAPIClient = weatherAPIClient;
        this.visualizationService = visualizationService;
    }

    @GetMapping("/weather/3d")
    public ResponseEntity<String> get3DWeather(@RequestParam String city) {
        try {
            if (city == null || city.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("City name is required");
            }

            WeatherInfo weatherInfo = weatherAPIClient.fetchWeather(city.trim());
            if (weatherInfo != null) {
                log.info("Generating 3D visualization for {}", city);
                
                String html3D = visualizationService.generate3DWeatherScene(city, weatherInfo);
                return ResponseEntity.ok().body(html3D);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error generating 3D weather for city: {}", city, e);
            return ResponseEntity.internalServerError()
                    .body("Error generating 3D weather visualization");
        }
    }

    @GetMapping("/weather/ai-description")
    public ResponseEntity<String> getAIWeatherDescription(@RequestParam String city) {
        try {
            WeatherInfo weatherInfo = weatherAPIClient.fetchWeather(city.trim());
            if (weatherInfo != null) {
                String aiDescription = visualizationService.generateAIDescription(city, weatherInfo);
                return ResponseEntity.ok()
                        .header("Content-Type", "text/plain; charset=utf-8")
                        .body(aiDescription);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Weather information not found for city: " + city);
            }
        } catch (Exception e) {
            log.error("Error generating AI description for city: {}", city, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to generate weather story at this time. Please try again later.");
        }
    }
}