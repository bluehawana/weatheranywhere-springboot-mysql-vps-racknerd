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

    public String generateLandmarkSVGPrompt(String city, WeatherInfo weatherInfo) {
        return String.format(
            "Generate SVG code for a simple, minimalist landmark icon of %s in black line-art style. " +
            "Requirements: " +
            "1. Create a simple SVG (100x100 viewBox) showing the most recognizable landmark of %s " +
            "2. Use only black lines/strokes (#000000) on transparent background, like a simple icon " +
            "3. Very minimalist style - similar to icons8 or simple line drawings " +
            "4. No fills, only stroke outlines, stroke-width around 2-3px " +
            "5. No text, no weather effects, just the pure landmark shape " +
            "6. Make it instantly recognizable (Eiffel Tower for Paris, Big Ben for London, etc.) " +
            "7. Return ONLY the SVG code, no explanations " +
            "Example format: <svg viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'><path d='...' fill='none' stroke='#000000' stroke-width='2'/></svg>",
            city, city
        );
    }

    public String generateLandmarkPNGPrompt(String city) {
        return String.format(
            "Create a simple, minimalist black line-art icon of the most famous landmark of %s. " +
            "Style requirements: " +
            "- Very simple black lines on white/transparent background " +
            "- Similar to icons8 or simple vector icons " +
            "- 100x100 pixels, PNG format " +
            "- No fills, only black outlines " +
            "- Instantly recognizable landmark (Eiffel Tower for Paris, Big Ben for London, etc.) " +
            "- Clean, minimal design suitable for use as weather app icon",
            city
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

    public String generateAILandmarkSVG(String city, WeatherInfo weatherInfo) {
        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            return generateFallbackLandmarkSVG(city, weatherInfo);
        }

        try {
            String prompt = generateLandmarkSVGPrompt(city, weatherInfo);
            String aiResponse = callOpenAI(prompt);
            
            // Extract SVG from AI response (it might include extra text)
            String svgCode = extractSVGFromResponse(aiResponse);
            return svgCode != null ? svgCode : generateFallbackLandmarkSVG(city, weatherInfo);
            
        } catch (Exception e) {
            System.err.println("AI landmark generation failed, using fallback: " + e.getMessage());
            return generateFallbackLandmarkSVG(city, weatherInfo);
        }
    }

    private String extractSVGFromResponse(String response) {
        // Extract SVG code from AI response
        int svgStart = response.indexOf("<svg");
        int svgEnd = response.lastIndexOf("</svg>") + 6;
        
        if (svgStart >= 0 && svgEnd > svgStart) {
            return response.substring(svgStart, svgEnd);
        }
        
        return null;
    }

    private String generateFallbackLandmarkSVG(String city, WeatherInfo weatherInfo) {
        // Simple fallback SVG with city name and weather emoji
        String weatherEmoji = getWeatherEmoji(weatherInfo.getDescription());
        
        return String.format("""
            <svg viewBox="0 0 400 300" style="background: linear-gradient(to bottom, #87CEEB, #98FB98);">
                <rect x="150" y="120" width="100" height="80" fill="#696969" stroke="#333" stroke-width="2"/>
                <polygon points="150,120 200,80 250,120" fill="#8B4513"/>
                <rect x="180" y="150" width="15" height="25" fill="#654321"/>
                <rect x="205" y="150" width="15" height="25" fill="#654321"/>
                <text x="200" y="50" text-anchor="middle" font-size="40">%s</text>
                <text x="200" y="280" text-anchor="middle" font-size="16" fill="#333">%s</text>
                <text x="200" y="250" text-anchor="middle" font-size="14" fill="#666">%.1f°C • %s</text>
            </svg>
            """, weatherEmoji, city, weatherInfo.getTemperature(), weatherInfo.getDescription());
    }

    private String getWeatherEmoji(String description) {
        String desc = description.toLowerCase();
        if (desc.contains("clear") || desc.contains("sunny")) return "☀️";
        if (desc.contains("rain")) return "🌧️";
        if (desc.contains("snow")) return "❄️";
        if (desc.contains("cloud")) return "☁️";
        if (desc.contains("storm")) return "⛈️";
        if (desc.contains("fog") || desc.contains("mist")) return "🌫️";
        return "🌤️";
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