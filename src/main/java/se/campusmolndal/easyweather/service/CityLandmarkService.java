package se.campusmolndal.easyweather.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

@Service
public class CityLandmarkService {

    @Value("${noun.project.api.key:}")
    private String nounProjectApiKey;
    private final HttpClient httpClient;

    public CityLandmarkService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String getCityIcon(String cityName) {
        // Try to get specific landmark first, fallback to city name + landmark
        String iconTerm = getOptimalCityTerm(cityName);
        String result = getNounProjectIcon(iconTerm, 64);
        
        // If specific landmark fails, try just the city name
        if (result.contains("📍")) {
            result = getNounProjectIcon(cityName, 64);
        }
        
        // If city name fails, try "city landmark"
        if (result.contains("📍")) {
            result = getNounProjectIcon(cityName + " landmark", 64);
        }
        
        return result;
    }
    
    private String getOptimalCityTerm(String cityName) {
        return switch (cityName.toLowerCase()) {
            case "london" -> "big ben";
            case "paris" -> "eiffel tower";
            case "tokyo" -> "tokyo tower";
            case "new york", "newyork" -> "statue of liberty";
            case "sydney" -> "opera house";
            case "beijing" -> "forbidden city";
            case "shanghai" -> "oriental pearl tower";
            case "hong kong" -> "hong kong skyline";
            case "dubai" -> "burj khalifa";
            case "mumbai" -> "gateway of india";
            case "moscow" -> "red square";
            case "rome" -> "colosseum";
            case "athens" -> "parthenon";
            case "cairo" -> "pyramid";
            case "rio de janeiro", "rio" -> "christ redeemer";
            case "barcelona" -> "sagrada familia";
            case "amsterdam" -> "windmill";
            case "berlin" -> "brandenburg gate";
            case "istanbul" -> "hagia sophia";
            default -> cityName + " landmark";
        };
    }

    public String getWeatherIcon(String weatherDescription) {
        if (weatherDescription == null || weatherDescription.isEmpty()) {
            return getNounProjectIcon("weather", 64);
        }
        
        String iconTerm = getOptimalWeatherTerm(weatherDescription);
        String result = getNounProjectIcon(iconTerm, 64);
        
        // If specific weather term fails, try the original description
        if (result.contains("📍")) {
            result = getNounProjectIcon(weatherDescription, 64);
        }
        
        // If original description fails, try "weather"
        if (result.contains("📍")) {
            result = getNounProjectIcon("weather", 64);
        }
        
        return result;
    }
    
    private String getOptimalWeatherTerm(String weatherDescription) {
        String lowercaseDesc = weatherDescription.toLowerCase();
        
        // Specific weather condition mapping
        if (lowercaseDesc.contains("thunderstorm") || lowercaseDesc.contains("thunder")) {
            return "thunderstorm";
        } else if (lowercaseDesc.contains("lightning")) {
            return "lightning";
        } else if (lowercaseDesc.contains("hail")) {
            return "hail";
        } else if (lowercaseDesc.contains("tornado")) {
            return "tornado";
        } else if (lowercaseDesc.contains("hurricane")) {
            return "hurricane";
        } else if (lowercaseDesc.contains("blizzard")) {
            return "blizzard";
        } else if (lowercaseDesc.contains("heavy rain") || lowercaseDesc.contains("downpour")) {
            return "heavy rain";
        } else if (lowercaseDesc.contains("light rain") || lowercaseDesc.contains("drizzle")) {
            return "light rain";
        } else if (lowercaseDesc.contains("rain") || lowercaseDesc.contains("shower")) {
            return "rain";
        } else if (lowercaseDesc.contains("heavy snow")) {
            return "heavy snow";
        } else if (lowercaseDesc.contains("light snow")) {
            return "light snow";
        } else if (lowercaseDesc.contains("snow")) {
            return "snow";
        } else if (lowercaseDesc.contains("sleet")) {
            return "sleet";
        } else if (lowercaseDesc.contains("freezing")) {
            return "freezing";
        } else if (lowercaseDesc.contains("clear") || lowercaseDesc.contains("sunny")) {
            return "sun";
        } else if (lowercaseDesc.contains("partly cloudy") || lowercaseDesc.contains("partly")) {
            return "partly cloudy";
        } else if (lowercaseDesc.contains("mostly cloudy") || lowercaseDesc.contains("overcast")) {
            return "overcast";
        } else if (lowercaseDesc.contains("cloud")) {
            return "cloud";
        } else if (lowercaseDesc.contains("fog") || lowercaseDesc.contains("mist")) {
            return "fog";
        } else if (lowercaseDesc.contains("haze") || lowercaseDesc.contains("hazy")) {
            return "haze";
        } else if (lowercaseDesc.contains("windy") || lowercaseDesc.contains("wind")) {
            return "wind";
        } else if (lowercaseDesc.contains("hot") || lowercaseDesc.contains("heat")) {
            return "hot weather";
        } else if (lowercaseDesc.contains("cold") || lowercaseDesc.contains("freezing")) {
            return "cold weather";
        } else {
            // Default fallback - try the original description
            return weatherDescription;
        }
    }

    private String getNounProjectIcon(String term, int size) {
        try {
            String url = String.format("https://api.thenounproject.com/v2/icon?query=%s&limit=1", 
                                     term.replace(" ", "%20"));
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + nounProjectApiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                // Simple parsing - in real implementation would use JSON parser
                String body = response.body();
                if (body.contains("\"icon_url\"")) {
                    String iconUrl = extractIconUrl(body);
                    if (iconUrl != null) {
                        return String.format("<img src='%s' alt='%s' style='width: %dpx; height: %dpx;' />", 
                                           iconUrl, term, size, size);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch icon for: " + term);
        }
        
        // Fallback to simple text
        return String.format("<span style='font-size: %dpx;'>📍 %s</span>", size/2, term);
    }

    private String extractIconUrl(String jsonResponse) {
        // Simple string extraction - would use proper JSON parser in production
        int start = jsonResponse.indexOf("\"icon_url\":\"");
        if (start > 0) {
            start += 12; // length of "icon_url":"
            int end = jsonResponse.indexOf("\"", start);
            if (end > start) {
                return jsonResponse.substring(start, end);
            }
        }
        return null;
    }
}