package se.campusmolndal.easyweather.service;

import org.springframework.stereotype.Service;
import se.campusmolndal.easyweather.models.WeatherInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

@Service
public class WeatherAnimationService {
    
    // City background images (PNG)
    private static final Map<String, String> CITY_BACKGROUNDS = new HashMap<>();
    
    // Weather animation overlays (GIF/TIF)
    private static final Map<String, WeatherAnimation> WEATHER_ANIMATIONS = new HashMap<>();
    
    static {
        // Initialize city backgrounds
        CITY_BACKGROUNDS.put("new york", "/images/cities/new-york.png");
        CITY_BACKGROUNDS.put("paris", "/images/cities/paris.png");
        CITY_BACKGROUNDS.put("london", "/images/cities/london.png");
        CITY_BACKGROUNDS.put("tokyo", "/images/cities/tokyo.png");
        CITY_BACKGROUNDS.put("stockholm", "/images/cities/stockholm.png");
        CITY_BACKGROUNDS.put("singapore", "/images/cities/singapore.png");
        CITY_BACKGROUNDS.put("mohe", "/images/cities/mohe.png");
        CITY_BACKGROUNDS.put("reykjavik", "/images/cities/reykjavik.png");
        CITY_BACKGROUNDS.put("sydney", "/images/cities/sydney.png");
        CITY_BACKGROUNDS.put("dubai", "/images/cities/dubai.png");
        
        // Initialize weather animations
        WEATHER_ANIMATIONS.put("clear", new WeatherAnimation("clear", "/animations/weather/sunny.gif", "☀️", "#FFD700"));
        WEATHER_ANIMATIONS.put("rain", new WeatherAnimation("rain", "/animations/weather/rain.gif", "🌧️", "#4682B4"));
        WEATHER_ANIMATIONS.put("heavy-rain", new WeatherAnimation("heavy-rain", "/animations/weather/heavy-rain.gif", "🌧️", "#1E90FF"));
        WEATHER_ANIMATIONS.put("snow", new WeatherAnimation("snow", "/animations/weather/snow.gif", "❄️", "#E6E6FA"));
        WEATHER_ANIMATIONS.put("heavy-snow", new WeatherAnimation("heavy-snow", "/animations/weather/heavy-snow.gif", "❄️", "#B0E0E6"));
        WEATHER_ANIMATIONS.put("cloudy", new WeatherAnimation("cloudy", "/animations/weather/clouds.gif", "☁️", "#87CEEB"));
        WEATHER_ANIMATIONS.put("partly-cloudy", new WeatherAnimation("partly-cloudy", "/animations/weather/partly-cloudy.gif", "⛅", "#87CEFA"));
        WEATHER_ANIMATIONS.put("thunderstorm", new WeatherAnimation("thunderstorm", "/animations/weather/thunderstorm.gif", "⛈️", "#483D8B"));
        WEATHER_ANIMATIONS.put("fog", new WeatherAnimation("fog", "/animations/weather/fog.gif", "🌫️", "#D3D3D3"));
        WEATHER_ANIMATIONS.put("drizzle", new WeatherAnimation("drizzle", "/animations/weather/drizzle.gif", "🌦️", "#6495ED"));
        WEATHER_ANIMATIONS.put("windy", new WeatherAnimation("windy", "/animations/weather/wind.gif", "💨", "#B0C4DE"));
    }
    
    public WeatherComposition generateWeatherComposition(String city, WeatherInfo weatherInfo) {
        String cityKey = city.toLowerCase().trim();
        String weatherCondition = mapWeatherCodeToAnimation(weatherInfo.getWeatherCode());
        
        // Get city background
        String cityBackground = CITY_BACKGROUNDS.getOrDefault(cityKey, "/images/cities/default.png");
        
        // Get weather animation
        WeatherAnimation weatherAnimation = WEATHER_ANIMATIONS.getOrDefault(weatherCondition, 
            WEATHER_ANIMATIONS.get("clear"));
        
        return new WeatherComposition(city, cityBackground, weatherAnimation, weatherInfo);
    }
    
    private String mapWeatherCodeToAnimation(int weatherCode) {
        return switch (weatherCode) {
            case 0 -> "clear";
            case 1 -> "partly-cloudy";
            case 2, 3 -> "cloudy";
            case 45, 48 -> "fog";
            case 51, 53, 55 -> "drizzle";
            case 61, 63 -> "rain";
            case 65 -> "heavy-rain";
            case 71, 73 -> "snow";
            case 75 -> "heavy-snow";
            case 80, 81, 82 -> "rain";
            case 95, 96, 99 -> "thunderstorm";
            default -> "clear";
        };
    }
    
    public String generateAnimatedWeatherHTML(String city, WeatherInfo weatherInfo) {
        WeatherComposition composition = generateWeatherComposition(city, weatherInfo);
        
        return String.format("""
            <div class="weather-composition" style="position: relative; width: 400px; height: 300px; border-radius: 15px; overflow: hidden; box-shadow: 0 8px 32px rgba(0,0,0,0.3);">
                <!-- City Background -->
                <img src="%s" alt="%s" style="position: absolute; top: 0; left: 0; width: 100%%; height: 100%%; object-fit: cover; z-index: 1;" 
                     onerror="this.src='/images/cities/default.png';">
                
                <!-- Weather Animation Overlay -->
                <img src="%s" alt="%s weather" style="position: absolute; top: 0; left: 0; width: 100%%; height: 100%%; object-fit: cover; z-index: 2; mix-blend-mode: multiply; opacity: 0.8;" 
                     onerror="this.style.display='none';">
                
                <!-- Weather Info Overlay -->
                <div style="position: absolute; bottom: 0; left: 0; right: 0; background: linear-gradient(transparent, rgba(0,0,0,0.7)); padding: 20px; z-index: 3; color: white;">
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <div>
                            <h3 style="margin: 0; font-size: 1.5rem;">%s</h3>
                            <p style="margin: 5px 0; opacity: 0.9;">%s</p>
                        </div>
                        <div style="text-align: right;">
                            <div style="font-size: 2.5rem; margin-bottom: 5px;">%s</div>
                            <div style="font-size: 1.8rem; font-weight: bold;">%.1f°C</div>
                        </div>
                    </div>
                </div>
                
                <!-- Animated Weather Icon -->
                <div style="position: absolute; top: 15px; right: 15px; z-index: 4; font-size: 2rem; animation: float 3s ease-in-out infinite;">
                    %s
                </div>
            </div>
            
            <style>
                @keyframes float {
                    0%%, 100%% { transform: translateY(0px); }
                    50%% { transform: translateY(-10px); }
                }
                
                .weather-composition:hover {
                    transform: scale(1.02);
                    transition: transform 0.3s ease;
                }
            </style>
            """, 
            composition.getCityBackground(),
            composition.getCityName(),
            composition.getWeatherAnimation().getAnimationPath(),
            composition.getWeatherAnimation().getName(),
            composition.getCityName(),
            composition.getWeatherInfo().getDescription(),
            composition.getWeatherAnimation().getEmoji(),
            composition.getWeatherInfo().getTemperature(),
            composition.getWeatherAnimation().getEmoji()
        );
    }
    
    public String generateCSSAnimatedWeather(String city, WeatherInfo weatherInfo) {
        WeatherComposition composition = generateWeatherComposition(city, weatherInfo);
        String weatherType = composition.getWeatherAnimation().getName();
        
        return String.format("""
            <div class="css-weather-scene" data-weather="%s" style="position: relative; width: 400px; height: 300px; border-radius: 15px; overflow: hidden; background: linear-gradient(135deg, %s, #ffffff);">
                <!-- City Silhouette -->
                <div class="city-silhouette" style="position: absolute; bottom: 0; left: 0; right: 0; height: 40%%; background: url('%s') no-repeat center bottom; background-size: cover; z-index: 1; filter: brightness(0.3);"></div>
                
                <!-- CSS Weather Animation -->
                %s
                
                <!-- Weather Info -->
                <div style="position: absolute; top: 20px; left: 20px; z-index: 10; color: white; text-shadow: 2px 2px 4px rgba(0,0,0,0.5);">
                    <h3 style="margin: 0; font-size: 1.3rem;">%s</h3>
                    <p style="margin: 5px 0; font-size: 1rem;">%.1f°C • %s</p>
                    <div style="font-size: 1.5rem; margin-top: 5px;">%s</div>
                </div>
            </div>
            """,
            weatherType,
            composition.getWeatherAnimation().getColor(),
            composition.getCityBackground(),
            generateCSSWeatherEffect(weatherType),
            composition.getCityName(),
            composition.getWeatherInfo().getTemperature(),
            composition.getWeatherInfo().getDescription(),
            composition.getWeatherAnimation().getEmoji()
        );
    }
    
    private String generateCSSWeatherEffect(String weatherType) {
        return switch (weatherType) {
            case "rain", "heavy-rain" -> generateRainEffect();
            case "snow", "heavy-snow" -> generateSnowEffect();
            case "clear" -> generateSunEffect();
            case "cloudy", "partly-cloudy" -> generateCloudEffect();
            case "thunderstorm" -> generateThunderstormEffect();
            case "fog" -> generateFogEffect();
            default -> "";
        };
    }
    
    private String generateRainEffect() {
        return """
            <div class="rain-container" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; z-index: 5; pointer-events: none;">
                <div class="rain" style="position: absolute; top: -100px; left: 0; width: 100%; height: 120%; background: repeating-linear-gradient(90deg, transparent, transparent 2px, rgba(255,255,255,0.6) 2px, rgba(255,255,255,0.6) 4px); animation: rain-fall 0.5s linear infinite;"></div>
            </div>
            <style>
                @keyframes rain-fall {
                    0% { transform: translateY(-100px) translateX(0); }
                    100% { transform: translateY(400px) translateX(-50px); }
                }
            </style>
            """;
    }
    
    private String generateSnowEffect() {
        return """
            <div class="snow-container" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; z-index: 5; pointer-events: none;">
                <div class="snowflake" style="position: absolute; color: white; font-size: 1rem; animation: snow-fall 3s linear infinite; animation-delay: 0s;">❄</div>
                <div class="snowflake" style="position: absolute; color: white; font-size: 0.8rem; animation: snow-fall 4s linear infinite; animation-delay: 1s; left: 20%;">❅</div>
                <div class="snowflake" style="position: absolute; color: white; font-size: 1.2rem; animation: snow-fall 3.5s linear infinite; animation-delay: 2s; left: 40%;">❄</div>
                <div class="snowflake" style="position: absolute; color: white; font-size: 0.9rem; animation: snow-fall 4.5s linear infinite; animation-delay: 0.5s; left: 60%;">❅</div>
                <div class="snowflake" style="position: absolute; color: white; font-size: 1.1rem; animation: snow-fall 3.8s linear infinite; animation-delay: 1.5s; left: 80%;">❄</div>
            </div>
            <style>
                @keyframes snow-fall {
                    0% { transform: translateY(-100px) rotate(0deg); opacity: 1; }
                    100% { transform: translateY(400px) rotate(360deg); opacity: 0; }
                }
            </style>
            """;
    }
    
    private String generateSunEffect() {
        return """
            <div class="sun-container" style="position: absolute; top: 30px; right: 30px; z-index: 5;">
                <div class="sun" style="width: 60px; height: 60px; background: radial-gradient(circle, #FFD700, #FFA500); border-radius: 50%; animation: sun-glow 2s ease-in-out infinite alternate; box-shadow: 0 0 20px rgba(255, 215, 0, 0.6);"></div>
            </div>
            <style>
                @keyframes sun-glow {
                    0% { box-shadow: 0 0 20px rgba(255, 215, 0, 0.6); }
                    100% { box-shadow: 0 0 30px rgba(255, 215, 0, 0.9); }
                }
            </style>
            """;
    }
    
    private String generateCloudEffect() {
        return """
            <div class="clouds-container" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; z-index: 5; pointer-events: none;">
                <div class="cloud" style="position: absolute; top: 20%; left: -20%; width: 100px; height: 40px; background: rgba(255,255,255,0.8); border-radius: 40px; animation: cloud-move 15s linear infinite;"></div>
                <div class="cloud" style="position: absolute; top: 40%; left: -30%; width: 80px; height: 30px; background: rgba(255,255,255,0.6); border-radius: 30px; animation: cloud-move 20s linear infinite;"></div>
            </div>
            <style>
                @keyframes cloud-move {
                    0% { transform: translateX(-100px); }
                    100% { transform: translateX(500px); }
                }
            </style>
            """;
    }
    
    private String generateThunderstormEffect() {
        return """
            <div class="storm-container" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; z-index: 5; pointer-events: none;">
                <div class="lightning" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(255,255,255,0.9); opacity: 0; animation: lightning-flash 3s infinite;"></div>
            </div>
            <style>
                @keyframes lightning-flash {
                    0%, 90%, 100% { opacity: 0; }
                    5%, 10% { opacity: 1; }
                }
            </style>
            """;
    }
    
    private String generateFogEffect() {
        return """
            <div class="fog-container" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; z-index: 5; pointer-events: none;">
                <div class="fog" style="position: absolute; top: 0; left: 0; width: 120%; height: 100%; background: linear-gradient(90deg, rgba(255,255,255,0.3), rgba(200,200,200,0.5), rgba(255,255,255,0.3)); animation: fog-move 8s ease-in-out infinite;"></div>
            </div>
            <style>
                @keyframes fog-move {
                    0%, 100% { transform: translateX(-20%); }
                    50% { transform: translateX(0%); }
                }
            </style>
            """;
    }
    
    // Data classes
    public static class WeatherComposition {
        private final String cityName;
        private final String cityBackground;
        private final WeatherAnimation weatherAnimation;
        private final WeatherInfo weatherInfo;
        
        public WeatherComposition(String cityName, String cityBackground, WeatherAnimation weatherAnimation, WeatherInfo weatherInfo) {
            this.cityName = cityName;
            this.cityBackground = cityBackground;
            this.weatherAnimation = weatherAnimation;
            this.weatherInfo = weatherInfo;
        }
        
        // Getters
        public String getCityName() { return cityName; }
        public String getCityBackground() { return cityBackground; }
        public WeatherAnimation getWeatherAnimation() { return weatherAnimation; }
        public WeatherInfo getWeatherInfo() { return weatherInfo; }
    }
    
    public static class WeatherAnimation {
        private final String name;
        private final String animationPath;
        private final String emoji;
        private final String color;
        
        public WeatherAnimation(String name, String animationPath, String emoji, String color) {
            this.name = name;
            this.animationPath = animationPath;
            this.emoji = emoji;
            this.color = color;
        }
        
        // Getters
        public String getName() { return name; }
        public String getAnimationPath() { return animationPath; }
        public String getEmoji() { return emoji; }
        public String getColor() { return color; }
    }
    
    public List<String> getSupportedCities() {
        return Arrays.asList(
            "New York", "Paris", "London", "Tokyo", "Stockholm", 
            "Singapore", "Mohe", "Reykjavik", "Sydney", "Dubai"
        );
    }
    
    public List<String> getSupportedWeatherTypes() {
        return Arrays.asList(
            "clear", "rain", "heavy-rain", "snow", "heavy-snow", 
            "cloudy", "partly-cloudy", "thunderstorm", "fog", "drizzle", "windy"
        );
    }
}