package se.campusmolndal.easyweather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class GeocodingService {

    @Value("${opencage.api.key:}")
    private String openCageApiKey;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GeocodingService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public LocationInfo getLocationInfo(String cityName) {
        if (openCageApiKey == null || openCageApiKey.isEmpty()) {
            System.err.println("OpenCage API key not configured");
            return null;
        }

        try {
            String encodedCity = URLEncoder.encode(cityName, StandardCharsets.UTF_8);
            String url = String.format(
                "https://api.opencagedata.com/geocode/v1/json?q=%s&key=%s&limit=1&no_annotations=1&language=en",
                encodedCity, openCageApiKey
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "WeatherAnywhere/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseLocationFromResponse(response.body(), cityName);
            } else {
                System.err.println("OpenCage API error: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            System.err.println("Geocoding failed for " + cityName + ": " + e.getMessage());
        }

        return null;
    }

    private LocationInfo parseLocationFromResponse(String responseBody, String originalCityName) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode results = rootNode.get("results");

            if (results != null && results.isArray() && results.size() > 0) {
                JsonNode firstResult = results.get(0);
                JsonNode geometry = firstResult.get("geometry");
                JsonNode components = firstResult.get("components");

                if (geometry != null && components != null) {
                    double latitude = geometry.get("lat").asDouble();
                    double longitude = geometry.get("lng").asDouble();

                    // Extract location details
                    String country = getComponentValue(components, "country");
                    String state = getComponentValue(components, "state");
                    String city = getComponentValue(components, "city");
                    String formatted = firstResult.get("formatted").asText();

                    return new LocationInfo(
                        originalCityName,
                        latitude,
                        longitude,
                        country,
                        state,
                        city != null ? city : originalCityName,
                        formatted
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing geocoding response: " + e.getMessage());
        }

        return null;
    }

    private String getComponentValue(JsonNode components, String key) {
        JsonNode value = components.get(key);
        return value != null ? value.asText() : null;
    }

    public static class LocationInfo {
        private final String originalName;
        private final double latitude;
        private final double longitude;
        private final String country;
        private final String state;
        private final String city;
        private final String formattedAddress;

        public LocationInfo(String originalName, double latitude, double longitude, 
                           String country, String state, String city, String formattedAddress) {
            this.originalName = originalName;
            this.latitude = latitude;
            this.longitude = longitude;
            this.country = country;
            this.state = state;
            this.city = city;
            this.formattedAddress = formattedAddress;
        }

        // Getters
        public String getOriginalName() { return originalName; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public String getCountry() { return country; }
        public String getState() { return state; }
        public String getCity() { return city; }
        public String getFormattedAddress() { return formattedAddress; }

        @Override
        public String toString() {
            return String.format("LocationInfo{name='%s', lat=%.4f, lng=%.4f, country='%s', city='%s'}", 
                                originalName, latitude, longitude, country, city);
        }
    }
}