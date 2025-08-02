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

}