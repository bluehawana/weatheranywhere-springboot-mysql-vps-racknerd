package se.campusmolndal.easyweather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.campusmolndal.easyweather.models.WeatherInfo;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

@Service
public class AIWeatherService {

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AIWeatherService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String generateWeatherPrompt(String city, WeatherInfo weatherInfo) {
        return String.format(
            "Create a vivid, cinematic description for a 3D weather scene in %s. " +
            "Current conditions: Temperature %.1f°C, Wind speed %.1f m/s, Weather: %s. " +
            "Include atmospheric details, lighting, and environmental elements that would make " +
            "an immersive 3D visualization. Focus on the mood and visual aesthetics.",
            city, weatherInfo.getTemperature(), weatherInfo.getWindSpeed(), weatherInfo.getDescription()
        );
    }

    public String generateAIWeatherDescription(String city, WeatherInfo weatherInfo) {
        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            return generateFallbackDescription(city, weatherInfo);
        }

        try {
            String prompt = generateWeatherPrompt(city, weatherInfo);
            return callOpenAI(prompt);
        } catch (Exception e) {
            System.err.println("AI service failed, using fallback: " + e.getMessage());
            return generateFallbackDescription(city, weatherInfo);
        }
    }

    private String callOpenAI(String prompt) throws Exception {
        String requestBody = String.format("""
            {
                "model": "gpt-3.5-turbo",
                "messages": [
                    {
                        "role": "system",
                        "content": "You are a creative weather visualization expert who creates immersive, poetic descriptions for 3D weather scenes."
                    },
                    {
                        "role": "user",
                        "content": "%s"
                    }
                ],
                "max_tokens": 200,
                "temperature": 0.8
            }
            """, prompt.replace("\"", "\\\""));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Authorization", "Bearer " + openaiApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            try {
                JsonNode jsonResponse = objectMapper.readTree(response.body());
                JsonNode choices = jsonResponse.get("choices");
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    JsonNode message = choices.get(0).get("message");
                    if (message != null) {
                        JsonNode content = message.get("content");
                        if (content != null) {
                            return content.asText();
                        }
                    }
                }
                return "AI crafted a beautiful weather story, but there was an issue retrieving it.";
            } catch (Exception e) {
                System.err.println("Error parsing OpenAI response: " + e.getMessage());
                return "The AI is painting a vivid picture of the weather, but the canvas is temporarily unavailable.";
            }
        } else {
            System.err.println("OpenAI API error: " + response.statusCode() + " - " + response.body());
            throw new RuntimeException("OpenAI API call failed: " + response.statusCode());
        }
    }

    private String generateFallbackDescription(String city, WeatherInfo weatherInfo) {
        StringBuilder description = new StringBuilder();
        
        // City-specific enhancements
        if (city.toLowerCase().contains("mohe")) {
            description.append("In the northernmost reaches of China, Mohe stands as a sentinel of winter, ");
        } else if (city.toLowerCase().contains("stockholm")) {
            description.append("Across the Nordic waters of Stockholm, ");
        } else {
            description.append("In the heart of ").append(city).append(", ");
        }
        
        // Temperature-based atmosphere
        double temp = weatherInfo.getTemperature();
        if (temp < -10) {
            description.append("the arctic air crystallizes each breath into visible clouds, ");
        } else if (temp < 0) {
            description.append("winter's embrace brings a crisp clarity to the air, ");
        } else if (temp > 25) {
            description.append("golden sunlight warms the landscape with summer's gentle touch, ");
        } else {
            description.append("mild temperatures create the perfect canvas for nature's display, ");
        }
        
        // Weather-specific visuals
        String weather = weatherInfo.getDescription().toLowerCase();
        if (weather.contains("snow")) {
            description.append("as countless snowflakes perform their eternal dance, ")
                    .append("transforming the world into a pristine winter wonderland where ")
                    .append("every surface gleams with crystalline beauty.");
        } else if (weather.contains("rain")) {
            description.append("while rhythmic raindrops create nature's percussion, ")
                    .append("painting the streets with reflective mirrors that capture ")
                    .append("the city's lights in shimmering pools.");
        } else if (weather.contains("clear")) {
            description.append("under an endless azure dome where not a single cloud ")
                    .append("dares to interrupt the sun's magnificent performance, ")
                    .append("casting sharp shadows and brilliant highlights across the terrain.");
        } else {
            description.append("with ").append(weather).append(" creating a dynamic tapestry ")
                    .append("of light and shadow that shifts like a living painting ");
        }
        
        return description.toString();
    }

    public String generate3DSceneConfig(String city, WeatherInfo weatherInfo) {
        // Generate configuration for 3D scene based on weather
        StringBuilder config = new StringBuilder();
        config.append("{\n");
        config.append("  \"city\": \"").append(city).append("\",\n");
        config.append("  \"temperature\": ").append(weatherInfo.getTemperature()).append(",\n");
        config.append("  \"windSpeed\": ").append(weatherInfo.getWindSpeed()).append(",\n");
        config.append("  \"weather\": \"").append(weatherInfo.getDescription()).append("\",\n");
        
        // Determine scene parameters
        String weather = weatherInfo.getDescription().toLowerCase();
        if (weather.contains("snow")) {
            config.append("  \"particleType\": \"snow\",\n");
            config.append("  \"particleCount\": 2000,\n");
            config.append("  \"backgroundColor\": \"#E6F3FF\",\n");
            config.append("  \"lighting\": \"winter\"\n");
        } else if (weather.contains("rain")) {
            config.append("  \"particleType\": \"rain\",\n");
            config.append("  \"particleCount\": 3000,\n");
            config.append("  \"backgroundColor\": \"#708090\",\n");
            config.append("  \"lighting\": \"stormy\"\n");
        } else if (weather.contains("clear")) {
            config.append("  \"particleType\": \"none\",\n");
            config.append("  \"particleCount\": 0,\n");
            config.append("  \"backgroundColor\": \"#87CEEB\",\n");
            config.append("  \"lighting\": \"bright\"\n");
        } else {
            config.append("  \"particleType\": \"clouds\",\n");
            config.append("  \"particleCount\": 500,\n");
            config.append("  \"backgroundColor\": \"#B0C4DE\",\n");
            config.append("  \"lighting\": \"overcast\"\n");
        }
        
        config.append("}");
        return config.toString();
    }
}