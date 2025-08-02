package se.campusmolndal.easyweather.service;

import org.springframework.stereotype.Service;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

@Service
public class CityLandmarkService {

    private final String NOUN_PROJECT_API_KEY = "008adfb4f566422e8d88d5d74bdbe3e7";
    private final HttpClient httpClient;

    public CityLandmarkService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String getCityIcon(String cityName) {
        String iconTerm = switch (cityName.toLowerCase()) {
            case "london" -> "big ben";
            case "paris" -> "eiffel tower";
            case "tokyo" -> "tokyo tower";
            case "new york", "newyork" -> "statue of liberty";
            case "sydney" -> "opera house";
            default -> cityName + " landmark";
        };
        
        return getNounProjectIcon(iconTerm, 64);
    }

    public String getWeatherIcon(String weatherDescription) {
        if (weatherDescription == null || weatherDescription.isEmpty()) {
            return getNounProjectIcon("weather", 64);
        }
        
        String iconTerm = "";
        String lowercaseDesc = weatherDescription.toLowerCase();
        
        if (lowercaseDesc.contains("clear") || lowercaseDesc.contains("sunny")) {
            iconTerm = "sun";
        } else if (lowercaseDesc.contains("cloud") || lowercaseDesc.contains("overcast")) {
            iconTerm = "cloud";
        } else if (lowercaseDesc.contains("rain") || lowercaseDesc.contains("drizzle")) {
            iconTerm = "rain";
        } else if (lowercaseDesc.contains("snow")) {
            iconTerm = "snow";
        } else if (lowercaseDesc.contains("storm") || lowercaseDesc.contains("thunder")) {
            iconTerm = "storm";
        } else if (lowercaseDesc.contains("fog") || lowercaseDesc.contains("mist")) {
            iconTerm = "fog";
        } else if (lowercaseDesc.contains("wind")) {
            iconTerm = "wind";
        } else {
            iconTerm = "weather";
        }
        
        return getNounProjectIcon(iconTerm, 64);
    }

    private String getNounProjectIcon(String term, int size) {
        try {
            String url = String.format("https://api.thenounproject.com/v2/icon?query=%s&limit=1", 
                                     term.replace(" ", "%20"));
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + NOUN_PROJECT_API_KEY)
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