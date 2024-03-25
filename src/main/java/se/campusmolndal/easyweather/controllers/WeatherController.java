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

@RestController
public class WeatherController {

    private final WeatherAPIClient weatherAPIClient;
    private static final Logger log = LoggerFactory.getLogger(WeatherController.class);

    @Autowired
    public WeatherController(WeatherAPIClient weatherAPIClient) {
        this.weatherAPIClient = weatherAPIClient;
    }

    @GetMapping("/weather")
    public ResponseEntity<String> getWeather(@RequestParam String city) {
        WeatherInfo weatherInfo = weatherAPIClient.fetchWeather(city);
        if (weatherInfo != null) {
            log.info("Weather for {}: Temperature = {}, Wind Speed = {}, Description = {}",
                    city, weatherInfo.getTemperature(), weatherInfo.getWindSpeed(), weatherInfo.getDescription());

            String htmlContent = buildHtml(city, weatherInfo);
            return ResponseEntity.ok().body(htmlContent);
        } else {
            log.error("Failed to fetch weather for city: {}", city);
            return ResponseEntity.notFound().build();
        }
    }

    private String buildHtml(String city, WeatherInfo weatherInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div>");
        sb.append("<h2>Weather of ").append(city).append("</h2>");
        sb.append("<p>Temperature: ").append(weatherInfo.getTemperature()).append("°C</p>");
        sb.append("<p>Wind Speed: ").append(weatherInfo.getWindSpeed()).append(" m/s</p>");
        sb.append("<p>Description: ").append(weatherInfo.getDescription()).append("</p>");
        sb.append("</div>");
        return sb.toString();
    }
}
