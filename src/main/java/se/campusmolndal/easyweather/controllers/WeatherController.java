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
        
        // Add CSS for better styling
        sb.append("<style>")
          .append(".weather-container { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; }")
          .append(".weather-header { text-align: center; margin-bottom: 20px; }")
          .append(".weather-data { background: #f0f8ff; padding: 15px; border-radius: 10px; margin-bottom: 20px; }")
          .append(".landmark-section { text-align: center; }")
          .append("</style>");
        
        sb.append("<div class='weather-container'>");
        sb.append("<div class='weather-header'>");
        sb.append("<h2>Weather of ").append(city).append("</h2>");
        sb.append("</div>");
        
        sb.append("<div class='weather-data'>");
        sb.append("<p><strong>Temperature:</strong> ").append(weatherInfo.getTemperature()).append("°C</p>");
        sb.append("<p><strong>Wind Speed:</strong> ").append(weatherInfo.getWindSpeed()).append(" m/s</p>");
        sb.append("<p><strong>Description:</strong> ").append(weatherInfo.getDescription()).append("</p>");
        sb.append("</div>");
        
        // Add landmark animation section
        sb.append("<div class='landmark-section'>");
        sb.append("<div id='landmark-animation'>Loading landmark...</div>");
        sb.append("<script>");
        sb.append("fetch('/weather/landmark?city=").append(city).append("')")
          .append(".then(response => response.text())")
          .append(".then(html => document.getElementById('landmark-animation').innerHTML = html)")
          .append(".catch(error => document.getElementById('landmark-animation').innerHTML = '<p>Landmark animation unavailable</p>');");
        sb.append("</script>");
        sb.append("</div>");
        
        sb.append("</div>");
        return sb.toString();
    }
}