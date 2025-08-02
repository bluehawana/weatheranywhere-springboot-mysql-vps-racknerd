package se.campusmolndal.easyweather.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.campusmolndal.easyweather.models.WeatherInfo;
import se.campusmolndal.easyweather.controllers.WeatherAPIClient;
import se.campusmolndal.easyweather.service.CityLandmarkService;

@RestController
public class WeatherController {

    private final WeatherAPIClient weatherAPIClient;
    private final CityLandmarkService cityLandmarkService;
    private static final Logger log = LoggerFactory.getLogger(WeatherController.class);

    @Autowired
    public WeatherController(WeatherAPIClient weatherAPIClient, CityLandmarkService cityLandmarkService) {
        this.weatherAPIClient = weatherAPIClient;
        this.cityLandmarkService = cityLandmarkService;
    }

    @GetMapping({"/weather", "/api/weather"})
    public ResponseEntity<String> getWeather(@RequestParam String city) {
        try {
            if (city == null || city.trim().isEmpty()) {
                log.warn("Empty city parameter received");
                return ResponseEntity.badRequest().body("<p>City name is required</p>");
            }

            WeatherInfo weatherInfo = weatherAPIClient.fetchWeather(city.trim());
            if (weatherInfo != null) {
                log.info("Weather for {}: Temperature = {}, Wind Speed = {}, Description = {}",
                        city, weatherInfo.getTemperature(), weatherInfo.getWindSpeed(), weatherInfo.getDescription());

                String htmlContent = buildHtml(city, weatherInfo);
                return ResponseEntity.ok().body(htmlContent);
            } else {
                log.error("Failed to fetch weather for city: {}", city);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("<p>Weather information not found for city: " + city + "</p>");
            }
        } catch (Exception e) {
            log.error("Error fetching weather for city: {}", city, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("<p>Error retrieving weather information. Please try again later.</p>");
        }
    }

    private String buildHtml(String city, WeatherInfo weatherInfo) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("<h2>Weather of ").append(city).append("</h2>");
        sb.append("<p>Temperature: ").append(weatherInfo.getTemperature()).append("°C</p>");
        sb.append("<p>Wind Speed: ").append(weatherInfo.getWindSpeed()).append(" m/s</p>");
        sb.append("<p>Description: ").append(weatherInfo.getDescription()).append("</p>");
        
        return sb.toString();
    }
}