package se.campusmolndal.easyweather.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.campusmolndal.easyweather.models.WeatherInfo;
import java.util.Map;
import java.util.HashMap;

@Service
public class CityLandmarkService {

    @Autowired
    private AIWeatherService aiWeatherService;
    
    @Autowired 
    private WeatherIconService weatherIconService;

    // Cache for generated landmarks to avoid repeated API calls
    private final Map<String, String> landmarkCache = new HashMap<>();

    public String getCityLandmarkASCII(String cityName) {
        return switch (cityName.toLowerCase()) {
            case "paris" -> """
                <pre>
                    /\\
                   /  \\
                  /____\\
                 /      \\
                /________\\
               /          \\
              /______________\\
                Eiffel Tower
                </pre>""";
            case "london" -> """
                <pre>
                  ┌───┐
                  │ ○ │ Big
                  │ │ │ Ben
                  │ │ │
                  └─┬─┘
                    │
                  ┌─┴─┐
                  └───┘
                </pre>""";
            case "tokyo" -> """
                <pre>
                    △
                   /│\\
                  /_│_\\
                    │
                   /│\\
                  /_│_\\
                    │
                 Tokyo Tower
                </pre>""";
            case "new york", "newyork" -> """
                <pre>
                   ♀
                  /│\\
                 / │ \\
                   │
                 ──┴──
                Statue
                </pre>""";
            case "sydney" -> """
                <pre>
                ∩     ∩     ∩
               ╱ ╲   ╱ ╲   ╱ ╲
              ╱   ╲ ╱   ╲ ╱   ╲
             ╱─────╲─────╲─────╲
             Opera House Sydney
                </pre>""";
            default -> String.format("""
                <pre>
                  ┌─────┐
                  │ %s │
                  └─────┘
                </pre>""", cityName);
        };
    }

    public String getWeatherASCII(String weatherDescription) {
        if (weatherDescription == null || weatherDescription.isEmpty()) {
            return "?";
        }
        
        String lowercaseDesc = weatherDescription.toLowerCase();
        
        if (lowercaseDesc.contains("clear") || lowercaseDesc.contains("sunny")) {
            return """
                <pre>
                    \\   |   /
                     \\  |  /
                   - - ☀ - -
                     /  |  \\
                    /   |   \\
                   Sunny
                </pre>""";
        }
        if (lowercaseDesc.contains("cloud") || lowercaseDesc.contains("overcast")) {
            return """
                <pre>
                   ☁  ☁  ☁
                  ☁  ☁  ☁
                   ☁  ☁  ☁
                   Cloudy
                </pre>""";
        }
        if (lowercaseDesc.contains("rain") || lowercaseDesc.contains("drizzle")) {
            return """
                <pre>
                   ☁ ☁ ☁
                  ☁ ☁ ☁
                   | | |
                   | | |
                   Rainy
                </pre>""";
        }
        if (lowercaseDesc.contains("snow")) {
            return """
                <pre>
                   ☁ ☁ ☁
                  ☁ ☁ ☁
                   * * *
                   * * *
                   Snowy
                </pre>""";
        }
        if (lowercaseDesc.contains("storm") || lowercaseDesc.contains("thunder")) {
            return """
                <pre>
                   ☁ ☁ ☁
                  ☁ ☁ ☁
                   ⚡ | |
                   | ⚡ |
                   Storm
                </pre>""";
        }
        if (lowercaseDesc.contains("fog") || lowercaseDesc.contains("mist")) {
            return """
                <pre>
                  ≡ ≡ ≡ ≡
                 ≡ ≡ ≡ ≡
                  ≡ ≡ ≡ ≡
                   Foggy
                </pre>""";
        }
        
        return """
            <pre>
             ? ? ?
            ? ? ? ?
             ? ? ?
            Weather
            </pre>""";
    }

    public String generateCombinedWeatherDisplay(String cityName, WeatherInfo weatherInfo) {
        String landmarkSVG = getCityLandmarkSVG(cityName, weatherInfo);
        String weatherIconPath = getWeatherIconPath(weatherInfo.getDescription());
        WeatherIconService.WeatherIcon weatherIcon = weatherIconService.getWeatherIcon(weatherInfo.getWeatherCode());

        StringBuilder html = new StringBuilder();
        
        html.append("<div class='weather-display-container' style='text-align: center; max-width: 600px; margin: 0 auto;'>");
        
        // Weather condition section
        html.append("<div class='weather-section' style='background: #f0f8ff; padding: 20px; border-radius: 10px; margin-bottom: 20px;'>");
        html.append("<h3>Current Weather</h3>");
        html.append("<div style='display: flex; align-items: center; justify-content: center; gap: 20px;'>");
        
        // Weather icon (GIF/static file)
        html.append("<div class='weather-icon'>");
        html.append("<img src='").append(weatherIconPath).append("' ");
        html.append("alt='").append(weatherInfo.getDescription()).append("' ");
        html.append("style='width: 80px; height: 80px; object-fit: contain;' ");
        html.append("onerror=\"this.src='").append(weatherIcon.getSvgUrl()).append("'; this.style.width='60px'; this.style.height='60px';\" />");
        html.append("</div>");
        
        // Weather details
        html.append("<div class='weather-details' style='text-align: left;'>");
        html.append("<p><strong>").append(weatherInfo.getDescription()).append("</strong></p>");
        html.append("<p>Temperature: ").append(weatherInfo.getTemperature()).append("°C</p>");
        html.append("<p>Wind: ").append(weatherInfo.getWindSpeed()).append(" m/s</p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</div>");
        
        // City landmark section
        html.append("<div class='landmark-section' style='background: #fff; padding: 20px; border-radius: 10px; border: 1px solid #ddd;'>");
        html.append("<h3>").append(cityName).append(" Landmark</h3>");
        html.append("<div style='display: inline-block; margin: 10px 0;'>");
        html.append(landmarkSVG);
        html.append("</div>");
        html.append("</div>");
        
        html.append("</div>");
        
        return html.toString();
    }

    private String generateFallbackLandmarkSVG(String cityName) {
        return String.format("""
            <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" style="width: 100px; height: 100px;">
                <rect x="35" y="60" width="30" height="25" fill="none" stroke="#000000" stroke-width="2"/>
                <polygon points="30,60 50,40 70,60" fill="none" stroke="#000000" stroke-width="2"/>
                <rect x="45" y="70" width="4" height="8" fill="none" stroke="#000000" stroke-width="1"/>
                <rect x="55" y="70" width="4" height="8" fill="none" stroke="#000000" stroke-width="1"/>
                <text x="50" y="95" text-anchor="middle" font-size="8" fill="#000000">%s</text>
            </svg>
            """, cityName);
    }
}