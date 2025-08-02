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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AIArtisticWeatherService {

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    // Cache for generated images to avoid regenerating the same content
    private final Map<String, String> imageCache = new ConcurrentHashMap<>();
    private final Map<String, String> animationCache = new ConcurrentHashMap<>();

    public AIArtisticWeatherService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String generateArtisticCityImage(String city, WeatherInfo weatherInfo) {
        String cacheKey = city.toLowerCase() + "_" + weatherInfo.getWeatherCode();
        
        if (imageCache.containsKey(cacheKey)) {
            return imageCache.get(cacheKey);
        }

        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            return generateFallbackCityArt(city, weatherInfo);
        }

        try {
            String prompt = createCityImagePrompt(city, weatherInfo);
            String imageUrl = generateImageWithDALLE(prompt);
            
            if (imageUrl != null) {
                String htmlResult = createArtisticImageHTML(city, imageUrl, weatherInfo);
                imageCache.put(cacheKey, htmlResult);
                return htmlResult;
            }
            
        } catch (Exception e) {
            System.err.println("AI city image generation failed: " + e.getMessage());
        }
        
        String fallback = generateFallbackCityArt(city, weatherInfo);
        imageCache.put(cacheKey, fallback);
        return fallback;
    }

    public String generateArtisticWeatherAnimation(String city, WeatherInfo weatherInfo) {
        String cacheKey = "weather_" + weatherInfo.getWeatherCode() + "_" + city.toLowerCase();
        
        if (animationCache.containsKey(cacheKey)) {
            return animationCache.get(cacheKey);
        }

        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            return generateFallbackWeatherAnimation(weatherInfo);
        }

        try {
            String prompt = createWeatherAnimationPrompt(city, weatherInfo);
            String animationDescription = callOpenAI(prompt);
            
            String htmlResult = createAnimatedWeatherHTML(city, animationDescription, weatherInfo);
            animationCache.put(cacheKey, htmlResult);
            return htmlResult;
            
        } catch (Exception e) {
            System.err.println("AI weather animation generation failed: " + e.getMessage());
        }
        
        String fallback = generateFallbackWeatherAnimation(weatherInfo);
        animationCache.put(cacheKey, fallback);
        return fallback;
    }

    private String createCityImagePrompt(String city, WeatherInfo weatherInfo) {
        return String.format("""
            Create a detailed prompt for DALL-E to generate a handdrawn, artistic illustration of %s with current weather conditions.
            
            Weather: %s, Temperature: %.1f°C, Wind: %.1f m/s
            
            Style requirements:
            - Watercolor or ink wash painting style
            - Handdrawn, artistic, not photorealistic
            - Include famous landmarks of %s
            - Incorporate weather effects: %s
            - Warm, inviting color palette
            - Paper texture background
            - Artistic, sketchy line quality
            - Size: landscape orientation, suitable for web display
            
            Generate a DALL-E prompt that will create this artistic city illustration with weather effects.
            """, 
            city, weatherInfo.getDescription(), weatherInfo.getTemperature(), 
            weatherInfo.getWindSpeed(), city, weatherInfo.getDescription());
    }

    private String createWeatherAnimationPrompt(String city, WeatherInfo weatherInfo) {
        return String.format("""
            Create CSS animation code for handdrawn-style weather effects for %s.
            
            Current weather: %s (code: %d)
            Temperature: %.1f°C
            Wind speed: %.1f m/s
            
            Requirements:
            1. Generate CSS animations that simulate handdrawn weather effects
            2. Use SVG elements with artistic, sketchy styles
            3. Include realistic weather particle movements
            4. Style should match watercolor/ink wash aesthetic
            5. Animations should loop seamlessly
            6. Include weather-appropriate colors and opacity
            7. Make it feel organic and hand-crafted, not digital
            
            Weather type: %s
            
            Return complete CSS and HTML code for the weather animation overlay.
            """, 
            city, weatherInfo.getDescription(), weatherInfo.getWeatherCode(),
            weatherInfo.getTemperature(), weatherInfo.getWindSpeed(),
            weatherInfo.getDescription());
    }

    private String generateImageWithDALLE(String prompt) throws Exception {
        // First, get the DALL-E prompt from GPT
        String dallePrompt = callOpenAI(prompt);
        
        // Then use DALL-E to generate the image
        String requestBody = String.format("""
            {
                "model": "dall-e-3",
                "prompt": "%s",
                "n": 1,
                "size": "1024x1024",
                "quality": "standard",
                "style": "natural"
            }
            """, dallePrompt.replace("\"", "\\\"").replace("\n", " "));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/images/generations"))
                .header("Authorization", "Bearer " + openaiApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            JsonNode jsonResponse = objectMapper.readTree(response.body());
            JsonNode data = jsonResponse.get("data");
            if (data != null && data.isArray() && data.size() > 0) {
                JsonNode imageData = data.get(0);
                JsonNode url = imageData.get("url");
                if (url != null) {
                    return url.asText();
                }
            }
        }
        
        return null;
    }

    private String callOpenAI(String prompt) throws Exception {
        String requestBody = String.format("""
            {
                "model": "gpt-4",
                "messages": [
                    {
                        "role": "system",
                        "content": "You are an expert digital artist and web developer who creates beautiful, handdrawn-style artwork and animations. You specialize in watercolor, ink wash, and sketchy artistic styles."
                    },
                    {
                        "role": "user",
                        "content": "%s"
                    }
                ],
                "max_tokens": 500,
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
        }
        
        throw new RuntimeException("OpenAI API call failed: " + response.statusCode());
    }

    private String createArtisticImageHTML(String city, String imageUrl, WeatherInfo weatherInfo) {
        return String.format("""
            <div class="ai-artistic-composition" style="position: relative; width: 450px; height: 350px; border-radius: 20px; overflow: hidden; box-shadow: 0 12px 40px rgba(0,0,0,0.2); border: 3px solid #8b7355; margin: 0 auto;">
                
                <!-- AI Generated City Art -->
                <img src="%s" alt="AI generated %s artwork" 
                     style="position: absolute; top: 0; left: 0; width: 100%%; height: 100%%; object-fit: cover; z-index: 1; filter: sepia(0.1) contrast(1.05);" 
                     onerror="this.parentElement.innerHTML='%s';">
                
                <!-- Artistic overlay with weather info -->
                <div style="position: absolute; bottom: 0; left: 0; right: 0; background: linear-gradient(transparent, rgba(0,0,0,0.8)); padding: 20px; z-index: 3; color: white; font-family: 'Caveat', cursive;">
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <div>
                            <h3 style="margin: 0; font-size: 1.6rem; text-shadow: 2px 2px 4px rgba(0,0,0,0.5);">%s</h3>
                            <p style="margin: 5px 0; opacity: 0.9; font-size: 1rem;">%s</p>
                        </div>
                        <div style="text-align: right;">
                            <div style="font-size: 2.5rem; margin-bottom: 5px; filter: drop-shadow(2px 2px 4px rgba(0,0,0,0.5));">%s</div>
                            <div style="font-size: 1.8rem; font-weight: bold; color: #FFD700; text-shadow: 1px 1px 2px rgba(0,0,0,0.5);">%.1f°C</div>
                        </div>
                    </div>
                </div>
                
                <!-- AI signature -->
                <div style="position: absolute; top: 10px; right: 10px; z-index: 4; background: rgba(0,0,0,0.6); padding: 5px 10px; border-radius: 15px; font-family: cursive; font-size: 0.7rem; color: white; opacity: 0.8;">
                    🤖 AI Generated Art
                </div>
            </div>
            """, 
            imageUrl, city, generateFallbackCityArt(city, weatherInfo),
            city, weatherInfo.getDescription(), getWeatherEmoji(weatherInfo.getWeatherCode()), 
            weatherInfo.getTemperature());
    }

    private String createAnimatedWeatherHTML(String city, String animationCode, WeatherInfo weatherInfo) {
        return String.format("""
            <div class="ai-weather-animation" style="position: relative; width: 450px; height: 350px; border-radius: 20px; overflow: hidden; background: linear-gradient(135deg, #f8f6f0, #e8e2d5); border: 3px solid #8b7355; margin: 0 auto;">
                
                <!-- AI Generated Weather Animation -->
                <div style="position: absolute; top: 0; left: 0; width: 100%%; height: 100%%; z-index: 2;">
                    %s
                </div>
                
                <!-- City name and weather info -->
                <div style="position: absolute; bottom: 15px; left: 15px; right: 15px; background: rgba(248, 246, 240, 0.95); padding: 15px; border-radius: 15px; border: 2px dashed #8b7355; z-index: 3; font-family: 'Kalam', cursive;">
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <div>
                            <h3 style="margin: 0; font-size: 1.4rem; color: #5d4e37;">%s</h3>
                            <p style="margin: 5px 0; color: #8b7355; font-size: 0.9rem; font-style: italic;">%s</p>
                        </div>
                        <div style="text-align: right;">
                            <div style="font-size: 2.2rem; margin-bottom: 5px;">%s</div>
                            <div style="font-size: 1.6rem; font-weight: bold; color: #d4af37;">%.1f°C</div>
                        </div>
                    </div>
                </div>
                
                <!-- AI signature -->
                <div style="position: absolute; top: 10px; right: 10px; z-index: 4; background: rgba(212, 175, 55, 0.8); padding: 5px 10px; border-radius: 15px; font-family: cursive; font-size: 0.7rem; color: white;">
                    🤖 AI Animated
                </div>
            </div>
            """, 
            animationCode, city, weatherInfo.getDescription(), 
            getWeatherEmoji(weatherInfo.getWeatherCode()), weatherInfo.getTemperature());
    }

    private String generateFallbackCityArt(String city, WeatherInfo weatherInfo) {
        return String.format("""
            <div style="position: relative; width: 450px; height: 350px; border-radius: 20px; overflow: hidden; background: linear-gradient(135deg, #f8f6f0, #e8e2d5); border: 3px solid #8b7355; margin: 0 auto;">
                <svg viewBox="0 0 400 300" style="width: 100%%; height: 100%%;">
                    <defs>
                        <filter id="artisticTexture">
                            <feTurbulence baseFrequency="0.04" numOctaves="5" result="noise"/>
                            <feDiffuseLighting in="noise" lighting-color="white" surfaceScale="1">
                                <feDistantLight azimuth="45" elevation="60"/>
                            </feDiffuseLighting>
                        </filter>
                        <filter id="paintStroke">
                            <feTurbulence baseFrequency="0.3" numOctaves="2" result="noise"/>
                            <feDisplacementMap in="SourceGraphic" in2="noise" scale="2"/>
                        </filter>
                    </defs>
                    
                    <!-- Artistic background -->
                    <rect width="400" height="300" fill="#f8f6f0" filter="url(#artisticTexture)"/>
                    
                    <!-- Handdrawn city skyline -->
                    <path d="M0,250 L60,250 L60,200 L90,200 L90,170 L130,170 L130,150 L170,150 L170,130 L210,130 L210,110 L250,110 L250,140 L290,140 L290,170 L330,170 L330,190 L370,190 L370,210 L400,210 L400,300 L0,300 Z" 
                          fill="#5d4e37" 
                          stroke="#3d2e17" 
                          stroke-width="2" 
                          filter="url(#paintStroke)"
                          opacity="0.8"/>
                    
                    <!-- Weather effect -->
                    %s
                    
                    <!-- City name -->
                    <text x="200" y="280" text-anchor="middle" font-family="Caveat, cursive" font-size="20" fill="#5d4e37" transform="rotate(-1 200 280)">%s</text>
                    
                    <!-- Temperature display -->
                    <text x="350" y="50" text-anchor="middle" font-family="Kalam, cursive" font-size="24" fill="#d4af37" font-weight="bold">%.1f°C</text>
                    <text x="350" y="75" text-anchor="middle" font-family="Kalam, cursive" font-size="16" fill="#8b7355">%s</text>
                </svg>
                
                <!-- Fallback notice -->
                <div style="position: absolute; top: 10px; left: 10px; background: rgba(139, 115, 85, 0.8); padding: 5px 10px; border-radius: 15px; font-family: cursive; font-size: 0.7rem; color: white;">
                    🎨 Artistic Fallback
                </div>
            </div>
            """, 
            generateSimpleWeatherSVG(weatherInfo.getWeatherCode()),
            city, weatherInfo.getTemperature(), weatherInfo.getDescription());
    }

    private String generateFallbackWeatherAnimation(WeatherInfo weatherInfo) {
        return generateSimpleWeatherSVG(weatherInfo.getWeatherCode());
    }

    private String generateSimpleWeatherSVG(int weatherCode) {
        return switch (weatherCode) {
            case 61, 63, 65, 80, 81, 82 -> // Rain
                """
                <g stroke="#4682B4" stroke-width="2" opacity="0.6">
                    <line x1="50" y1="0" x2="45" y2="80">
                        <animateTransform attributeName="transform" type="translate" values="0,-50; 0,350" dur="1s" repeatCount="indefinite"/>
                    </line>
                    <line x1="100" y1="0" x2="95" y2="80">
                        <animateTransform attributeName="transform" type="translate" values="0,-50; 0,350" dur="1.2s" repeatCount="indefinite"/>
                    </line>
                    <line x1="150" y1="0" x2="145" y2="80">
                        <animateTransform attributeName="transform" type="translate" values="0,-50; 0,350" dur="0.8s" repeatCount="indefinite"/>
                    </line>
                </g>
                """;
            case 71, 73, 75 -> // Snow
                """
                <g fill="#E6E6FA" font-size="16" opacity="0.8">
                    <text x="50" y="0">❄</text>
                    <animateTransform attributeName="transform" type="translate" values="0,-30; 0,330" dur="4s" repeatCount="indefinite"/>
                    <text x="150" y="0">❅</text>
                    <animateTransform attributeName="transform" type="translate" values="0,-30; 0,330" dur="5s" repeatCount="indefinite"/>
                    <text x="250" y="0">❄</text>
                    <animateTransform attributeName="transform" type="translate" values="0,-30; 0,330" dur="3.5s" repeatCount="indefinite"/>
                </g>
                """;
            case 0 -> // Clear
                """
                <circle cx="320" cy="60" r="25" fill="#FFD700" opacity="0.8">
                    <animate attributeName="r" values="25;30;25" dur="3s" repeatCount="indefinite"/>
                </circle>
                <g stroke="#FFD700" stroke-width="2" opacity="0.6">
                    <line x1="320" y1="20" x2="320" y2="35"/>
                    <line x1="360" y1="60" x2="345" y2="60"/>
                    <line x1="320" y1="100" x2="320" y2="85"/>
                    <line x1="280" y1="60" x2="295" y2="60"/>
                </g>
                """;
            default -> "";
        };
    }

    private String getWeatherEmoji(int weatherCode) {
        return switch (weatherCode) {
            case 0 -> "☀️";
            case 1 -> "🌤️";
            case 2, 3 -> "☁️";
            case 45, 48 -> "🌫️";
            case 51, 53, 55 -> "🌦️";
            case 61, 63, 65 -> "🌧️";
            case 71, 73, 75 -> "❄️";
            case 80, 81, 82 -> "🌦️";
            case 95, 96, 99 -> "⛈️";
            default -> "🌤️";
        };
    }

    public void clearCache() {
        imageCache.clear();
        animationCache.clear();
    }

    public int getCacheSize() {
        return imageCache.size() + animationCache.size();
    }
}