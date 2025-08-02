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
        // City-specific fallback landmarks
        String landmarkSVG = switch (city.toLowerCase()) {
            case "london" -> generateLondonBigBen();
            case "paris" -> generateParisTower();
            case "tokyo" -> generateTokyoTower();
            case "new york", "newyork" -> generateNYStatueOfLiberty();
            case "sydney" -> generateSydneyOperaHouse();
            default -> generateGenericLandmark(city);
        };
        
        return landmarkSVG;
    }
    
    private String generateLondonBigBen() {
        return """
            <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" style="width: 100px; height: 100px;">
                <rect x="40" y="20" width="20" height="60" fill="none" stroke="#000000" stroke-width="2"/>
                <rect x="35" y="75" width="30" height="15" fill="none" stroke="#000000" stroke-width="2"/>
                <polygon points="35,20 50,10 65,20" fill="none" stroke="#000000" stroke-width="2"/>
                <circle cx="50" cy="35" r="8" fill="none" stroke="#000000" stroke-width="1.5"/>
                <line x1="50" y1="35" x2="50" y2="30" stroke="#000000" stroke-width="1"/>
                <line x1="50" y1="35" x2="54" y2="35" stroke="#000000" stroke-width="1"/>
                <text x="50" y="95" text-anchor="middle" font-size="6" fill="#000000">Big Ben</text>
            </svg>
            """;
    }
    
    private String generateParisTower() {
        return """
            <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" style="width: 100px; height: 100px;">
                <polygon points="50,10 45,30 55,30" fill="none" stroke="#000000" stroke-width="2"/>
                <polygon points="40,30 60,30 45,50 55,50" fill="none" stroke="#000000" stroke-width="2"/>
                <polygon points="35,50 65,50 25,80 75,80" fill="none" stroke="#000000" stroke-width="2"/>
                <line x1="45" y1="30" x2="45" y2="50" stroke="#000000" stroke-width="1"/>
                <line x1="55" y1="30" x2="55" y2="50" stroke="#000000" stroke-width="1"/>
                <line x1="40" y1="40" x2="60" y2="40" stroke="#000000" stroke-width="1"/>
                <text x="50" y="95" text-anchor="middle" font-size="6" fill="#000000">Eiffel Tower</text>
            </svg>
            """;
    }
    
    private String generateTokyoTower() {
        return """
            <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" style="width: 100px; height: 100px;">
                <polygon points="50,15 40,35 60,35" fill="none" stroke="#000000" stroke-width="2"/>
                <polygon points="35,35 65,35 30,70 70,70" fill="none" stroke="#000000" stroke-width="2"/>
                <rect x="47" y="70" width="6" height="15" fill="none" stroke="#000000" stroke-width="2"/>
                <line x1="40" y1="50" x2="60" y2="50" stroke="#000000" stroke-width="1"/>
                <circle cx="50" cy="25" r="3" fill="none" stroke="#000000" stroke-width="1"/>
                <text x="50" y="95" text-anchor="middle" font-size="6" fill="#000000">Tokyo Tower</text>
            </svg>
            """;
    }
    
    private String generateNYStatueOfLiberty() {
        return """
            <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" style="width: 100px; height: 100px;">
                <ellipse cx="50" cy="45" rx="8" ry="12" fill="none" stroke="#000000" stroke-width="2"/>
                <rect x="45" y="55" width="10" height="25" fill="none" stroke="#000000" stroke-width="2"/>
                <circle cx="50" cy="35" r="5" fill="none" stroke="#000000" stroke-width="2"/>
                <line x1="40" y1="30" x2="50" y2="25" stroke="#000000" stroke-width="2"/>
                <polygon points="45,25 50,15 55,25" fill="none" stroke="#000000" stroke-width="1"/>
                <text x="50" y="95" text-anchor="middle" font-size="6" fill="#000000">Statue of Liberty</text>
            </svg>
            """;
    }
    
    private String generateSydneyOperaHouse() {
        return """
            <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" style="width: 100px; height: 100px;">
                <path d="M20,70 Q30,40 40,70" fill="none" stroke="#000000" stroke-width="2"/>
                <path d="M35,70 Q45,35 55,70" fill="none" stroke="#000000" stroke-width="2"/>
                <path d="M50,70 Q60,40 70,70" fill="none" stroke="#000000" stroke-width="2"/>
                <path d="M65,70 Q75,45 85,70" fill="none" stroke="#000000" stroke-width="2"/>
                <line x1="15" y1="70" x2="85" y2="70" stroke="#000000" stroke-width="2"/>
                <text x="50" y="90" text-anchor="middle" font-size="6" fill="#000000">Opera House</text>
            </svg>
            """;
    }
    
    private String generateGenericLandmark(String city) {
        return String.format("""
            <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" style="width: 100px; height: 100px;">
                <rect x="35" y="60" width="30" height="25" fill="none" stroke="#000000" stroke-width="2"/>
                <polygon points="30,60 50,40 70,60" fill="none" stroke="#000000" stroke-width="2"/>
                <rect x="45" y="70" width="4" height="8" fill="none" stroke="#000000" stroke-width="1"/>
                <rect x="55" y="70" width="4" height="8" fill="none" stroke="#000000" stroke-width="1"/>
                <text x="50" y="95" text-anchor="middle" font-size="8" fill="#000000">%s</text>
            </svg>
            """, city);
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