package se.campusmolndal.easyweather.service;

import org.springframework.stereotype.Service;
import se.campusmolndal.easyweather.models.WeatherInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

@Service
public class HanddrawnWeatherService {
    
    // Handdrawn city illustrations (PNG with artistic style)
    private static final Map<String, HanddrawnAsset> HANDDRAWN_CITIES = new HashMap<>();
    
    // Handdrawn weather animations (GIF/TIF with artistic style)
    private static final Map<String, HanddrawnWeatherAnimation> HANDDRAWN_WEATHER = new HashMap<>();
    
    static {
        // Initialize handdrawn city assets
        HANDDRAWN_CITIES.put("new york", new HanddrawnAsset("new-york", "/images/handdrawn/cities/new-york-sketch.png", "Watercolor skyline with Empire State Building"));
        HANDDRAWN_CITIES.put("paris", new HanddrawnAsset("paris", "/images/handdrawn/cities/paris-sketch.png", "Ink drawing of Eiffel Tower and Seine"));
        HANDDRAWN_CITIES.put("london", new HanddrawnAsset("london", "/images/handdrawn/cities/london-sketch.png", "Pencil sketch of Big Ben and Thames"));
        HANDDRAWN_CITIES.put("tokyo", new HanddrawnAsset("tokyo", "/images/handdrawn/cities/tokyo-sketch.png", "Brush painting of Mount Fuji and cityscape"));
        HANDDRAWN_CITIES.put("stockholm", new HanddrawnAsset("stockholm", "/images/handdrawn/cities/stockholm-sketch.png", "Watercolor of Gamla Stan and archipelago"));
        HANDDRAWN_CITIES.put("singapore", new HanddrawnAsset("singapore", "/images/handdrawn/cities/singapore-sketch.png", "Ink wash of Marina Bay Sands"));
        HANDDRAWN_CITIES.put("mohe", new HanddrawnAsset("mohe", "/images/handdrawn/cities/mohe-sketch.png", "Charcoal drawing of winter landscape"));
        HANDDRAWN_CITIES.put("reykjavik", new HanddrawnAsset("reykjavik", "/images/handdrawn/cities/reykjavik-sketch.png", "Watercolor of colorful houses and mountains"));
        
        // Initialize handdrawn weather animations
        HANDDRAWN_WEATHER.put("clear", new HanddrawnWeatherAnimation("sunny", "/animations/handdrawn/sunny-sketch.gif", "☀️", "#FFD700", "Hand-drawn sun with radiating rays"));
        HANDDRAWN_WEATHER.put("rain", new HanddrawnWeatherAnimation("rain", "/animations/handdrawn/rain-sketch.gif", "🌧️", "#4682B4", "Sketched raindrops falling"));
        HANDDRAWN_WEATHER.put("heavy-rain", new HanddrawnWeatherAnimation("heavy-rain", "/animations/handdrawn/heavy-rain-sketch.gif", "🌧️", "#1E90FF", "Dense hand-drawn rain"));
        HANDDRAWN_WEATHER.put("snow", new HanddrawnWeatherAnimation("snow", "/animations/handdrawn/snow-sketch.gif", "❄️", "#E6E6FA", "Delicate snowflake drawings"));
        HANDDRAWN_WEATHER.put("heavy-snow", new HanddrawnWeatherAnimation("heavy-snow", "/animations/handdrawn/heavy-snow-sketch.gif", "❄️", "#B0E0E6", "Thick snowfall sketch"));
        HANDDRAWN_WEATHER.put("cloudy", new HanddrawnWeatherAnimation("cloudy", "/animations/handdrawn/clouds-sketch.gif", "☁️", "#87CEEB", "Fluffy hand-drawn clouds"));
        HANDDRAWN_WEATHER.put("partly-cloudy", new HanddrawnWeatherAnimation("partly-cloudy", "/animations/handdrawn/partly-cloudy-sketch.gif", "⛅", "#87CEFA", "Sun peeking through sketched clouds"));
        HANDDRAWN_WEATHER.put("thunderstorm", new HanddrawnWeatherAnimation("thunderstorm", "/animations/handdrawn/storm-sketch.gif", "⛈️", "#483D8B", "Dramatic lightning sketches"));
        HANDDRAWN_WEATHER.put("fog", new HanddrawnWeatherAnimation("fog", "/animations/handdrawn/fog-sketch.gif", "🌫️", "#D3D3D3", "Misty watercolor washes"));
        HANDDRAWN_WEATHER.put("drizzle", new HanddrawnWeatherAnimation("drizzle", "/animations/handdrawn/drizzle-sketch.gif", "🌦️", "#6495ED", "Light pencil rain strokes"));
        HANDDRAWN_WEATHER.put("windy", new HanddrawnWeatherAnimation("windy", "/animations/handdrawn/wind-sketch.gif", "💨", "#B0C4DE", "Swirling wind lines"));
    }
    
    public String generateHanddrawnWeatherComposition(String city, WeatherInfo weatherInfo) {
        String cityKey = city.toLowerCase().trim();
        String weatherCondition = mapWeatherCodeToAnimation(weatherInfo.getWeatherCode());
        
        HanddrawnAsset cityAsset = HANDDRAWN_CITIES.getOrDefault(cityKey, 
            new HanddrawnAsset("default", "/images/handdrawn/cities/default-sketch.png", "Generic city sketch"));
        
        HanddrawnWeatherAnimation weatherAnimation = HANDDRAWN_WEATHER.getOrDefault(weatherCondition, 
            HANDDRAWN_WEATHER.get("clear"));
        
        return String.format("""
            <div class="handdrawn-weather-composition" style="position: relative; width: 450px; height: 350px; border-radius: 20px; overflow: hidden; box-shadow: 0 12px 40px rgba(0,0,0,0.2); background: #f8f6f0; border: 3px solid #8b7355;">
                
                <!-- Paper texture background -->
                <div style="position: absolute; top: 0; left: 0; width: 100%%; height: 100%%; background: url('data:image/svg+xml,<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\"><defs><filter id=\"paper\"><feTurbulence baseFrequency=\"0.04\" numOctaves=\"5\" result=\"noise\" seed=\"1\"/><feDiffuseLighting in=\"noise\" lighting-color=\"white\" surfaceScale=\"1\"><feDistantLight azimuth=\"45\" elevation=\"60\"/></feDiffuseLighting></filter></defs><rect width=\"100\" height=\"100\" fill=\"%%23f8f6f0\" filter=\"url(%%23paper)\"/></svg>') repeat; opacity: 0.3; z-index: 0;"></div>
                
                <!-- Handdrawn city background -->
                <img src="%s" alt="%s sketch" 
                     style="position: absolute; top: 0; left: 0; width: 100%%; height: 100%%; object-fit: cover; z-index: 1; filter: sepia(0.2) contrast(1.1);" 
                     onerror="this.src='%s';">
                
                <!-- Handdrawn weather animation overlay -->
                <img src="%s" alt="%s weather animation" 
                     style="position: absolute; top: 0; left: 0; width: 100%%; height: 100%%; object-fit: cover; z-index: 2; mix-blend-mode: multiply; opacity: 0.7;" 
                     onerror="this.innerHTML='%s';">
                
                <!-- Handdrawn style info panel -->
                <div style="position: absolute; bottom: 15px; left: 15px; right: 15px; background: rgba(248, 246, 240, 0.95); padding: 15px; border-radius: 15px; border: 2px dashed #8b7355; z-index: 3; font-family: 'Comic Sans MS', cursive, sans-serif;">
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <div>
                            <h3 style="margin: 0; font-size: 1.4rem; color: #5d4e37; text-shadow: 1px 1px 2px rgba(0,0,0,0.1);">%s</h3>
                            <p style="margin: 5px 0; color: #8b7355; font-size: 0.9rem; font-style: italic;">%s</p>
                            <p style="margin: 5px 0; color: #6b5b47; font-size: 0.85rem;">%s</p>
                        </div>
                        <div style="text-align: right;">
                            <div style="font-size: 2.2rem; margin-bottom: 5px; filter: drop-shadow(2px 2px 4px rgba(0,0,0,0.2));">%s</div>
                            <div style="font-size: 1.6rem; font-weight: bold; color: %s; text-shadow: 1px 1px 2px rgba(0,0,0,0.1);">%.1f°C</div>
                            <div style="font-size: 0.8rem; color: #8b7355; margin-top: 2px;">%.1f m/s wind</div>
                        </div>
                    </div>
                </div>
                
                <!-- Handdrawn decorative elements -->
                <div style="position: absolute; top: 10px; right: 10px; z-index: 4; font-size: 1.5rem; animation: gentle-sway 4s ease-in-out infinite; filter: drop-shadow(1px 1px 2px rgba(0,0,0,0.2));">
                    %s
                </div>
                
                <!-- Artist signature -->
                <div style="position: absolute; bottom: 5px; right: 10px; z-index: 4; font-family: cursive; font-size: 0.7rem; color: #8b7355; opacity: 0.7; transform: rotate(-2deg);">
                    ~ hand drawn with ❤️
                </div>
            </div>
            
            <style>
                @keyframes gentle-sway {
                    0%%, 100%% { transform: rotate(-2deg) translateY(0px); }
                    25%% { transform: rotate(1deg) translateY(-2px); }
                    50%% { transform: rotate(-1deg) translateY(-1px); }
                    75%% { transform: rotate(2deg) translateY(-3px); }
                }
                
                .handdrawn-weather-composition:hover {
                    transform: scale(1.02) rotate(0.5deg);
                    transition: transform 0.4s ease;
                    box-shadow: 0 16px 50px rgba(0,0,0,0.25);
                }
                
                .handdrawn-weather-composition::before {
                    content: '';
                    position: absolute;
                    top: -2px;
                    left: -2px;
                    right: -2px;
                    bottom: -2px;
                    background: linear-gradient(45deg, #d4af37, #8b7355, #d4af37);
                    border-radius: 22px;
                    z-index: -1;
                    opacity: 0.3;
                }
            </style>
            """,
            cityAsset.getImagePath(),
            cityAsset.getName(),
            generateFallbackCitySketch(city),
            weatherAnimation.getAnimationPath(),
            weatherAnimation.getName(),
            generateCSSHanddrawnWeather(weatherCondition),
            city,
            weatherInfo.getDescription(),
            cityAsset.getDescription(),
            weatherAnimation.getEmoji(),
            weatherAnimation.getColor(),
            weatherInfo.getTemperature(),
            weatherInfo.getWindSpeed(),
            weatherAnimation.getEmoji()
        );
    }
    
    private String generateFallbackCitySketch(String city) {
        // Generate CSS-based handdrawn city silhouette as fallback
        return String.format("""
            <svg viewBox="0 0 400 300" style="width: 100%%; height: 100%%;">
                <defs>
                    <filter id="roughPaper">
                        <feTurbulence baseFrequency="0.04" numOctaves="5" result="noise" seed="2"/>
                        <feDiffuseLighting in="noise" lighting-color="white" surfaceScale="1">
                            <feDistantLight azimuth="45" elevation="60"/>
                        </feDiffuseLighting>
                    </filter>
                    <filter id="pencilStroke">
                        <feTurbulence baseFrequency="0.5" numOctaves="3" result="noise"/>
                        <feDisplacementMap in="SourceGraphic" in2="noise" scale="2"/>
                    </filter>
                </defs>
                
                <!-- Paper background -->
                <rect width="400" height="300" fill="#f8f6f0" filter="url(#roughPaper)"/>
                
                <!-- Handdrawn city skyline -->
                <path d="M0,250 L50,250 L50,200 L80,200 L80,180 L120,180 L120,160 L160,160 L160,140 L200,140 L200,120 L240,120 L240,160 L280,160 L280,180 L320,180 L320,200 L360,200 L360,220 L400,220 L400,300 L0,300 Z" 
                      fill="#5d4e37" 
                      stroke="#3d2e17" 
                      stroke-width="2" 
                      filter="url(#pencilStroke)"
                      opacity="0.8"/>
                
                <!-- City name in handwritten style -->
                <text x="200" y="280" text-anchor="middle" font-family="cursive" font-size="18" fill="#5d4e37" transform="rotate(-1 200 280)">%s</text>
                
                <!-- Decorative sketchy elements -->
                <circle cx="320" cy="80" r="25" fill="none" stroke="#8b7355" stroke-width="2" stroke-dasharray="3,2" opacity="0.6"/>
                <path d="M300,70 Q320,60 340,70" fill="none" stroke="#8b7355" stroke-width="1.5" opacity="0.5"/>
            </svg>
            """, city);
    }
    
    private String generateCSSHanddrawnWeather(String weatherType) {
        return switch (weatherType) {
            case "rain", "heavy-rain" -> generateHanddrawnRain();
            case "snow", "heavy-snow" -> generateHanddrawnSnow();
            case "clear" -> generateHanddrawnSun();
            case "cloudy", "partly-cloudy" -> generateHanddrawnClouds();
            case "thunderstorm" -> generateHanddrawnThunderstorm();
            case "fog" -> generateHanddrawnFog();
            default -> "";
        };
    }
    
    private String generateHanddrawnRain() {
        return """
            <div class="handdrawn-rain" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; z-index: 2; pointer-events: none;">
                <svg width="100%" height="100%" style="position: absolute;">
                    <defs>
                        <filter id="roughen">
                            <feTurbulence baseFrequency="0.02" numOctaves="3" result="noise"/>
                            <feDisplacementMap in="SourceGraphic" in2="noise" scale="1"/>
                        </filter>
                    </defs>
                    <!-- Handdrawn rain lines -->
                    <g stroke="#4682B4" stroke-width="1.5" opacity="0.7" filter="url(#roughen)">
                        <line x1="50" y1="0" x2="45" y2="100" stroke-dasharray="2,1" style="animation: rain-sketch 1s linear infinite;">
                            <animateTransform attributeName="transform" type="translate" values="0,-100; 0,400" dur="1s" repeatCount="indefinite"/>
                        </line>
                        <line x1="100" y1="0" x2="95" y2="100" stroke-dasharray="2,1" style="animation: rain-sketch 1.2s linear infinite;">
                            <animateTransform attributeName="transform" type="translate" values="0,-100; 0,400" dur="1.2s" repeatCount="indefinite"/>
                        </line>
                        <line x1="150" y1="0" x2="145" y2="100" stroke-dasharray="2,1" style="animation: rain-sketch 0.8s linear infinite;">
                            <animateTransform attributeName="transform" type="translate" values="0,-100; 0,400" dur="0.8s" repeatCount="indefinite"/>
                        </line>
                    </g>
                </svg>
            </div>
            """;
    }
    
    private String generateHanddrawnSnow() {
        return """
            <div class="handdrawn-snow" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; z-index: 2; pointer-events: none;">
                <div class="sketch-snowflake" style="position: absolute; left: 10%; font-size: 1.2rem; color: #E6E6FA; animation: snow-sketch 4s linear infinite; font-family: serif;">❅</div>
                <div class="sketch-snowflake" style="position: absolute; left: 30%; font-size: 0.9rem; color: #E6E6FA; animation: snow-sketch 5s linear infinite 1s; font-family: serif;">❄</div>
                <div class="sketch-snowflake" style="position: absolute; left: 50%; font-size: 1.1rem; color: #E6E6FA; animation: snow-sketch 4.5s linear infinite 2s; font-family: serif;">❅</div>
                <div class="sketch-snowflake" style="position: absolute; left: 70%; font-size: 1rem; color: #E6E6FA; animation: snow-sketch 3.8s linear infinite 0.5s; font-family: serif;">❄</div>
                <div class="sketch-snowflake" style="position: absolute; left: 90%; font-size: 0.8rem; color: #E6E6FA; animation: snow-sketch 4.2s linear infinite 1.5s; font-family: serif;">❅</div>
            </div>
            <style>
                @keyframes snow-sketch {
                    0% { transform: translateY(-50px) rotate(0deg); opacity: 0; }
                    10% { opacity: 1; }
                    90% { opacity: 1; }
                    100% { transform: translateY(400px) rotate(360deg); opacity: 0; }
                }
            </style>
            """;
    }
    
    private String generateHanddrawnSun() {
        return """
            <div class="handdrawn-sun" style="position: absolute; top: 40px; right: 40px; z-index: 2;">
                <svg width="80" height="80" viewBox="0 0 80 80">
                    <defs>
                        <filter id="sketch">
                            <feTurbulence baseFrequency="0.3" numOctaves="2" result="noise"/>
                            <feDisplacementMap in="SourceGraphic" in2="noise" scale="1"/>
                        </filter>
                    </defs>
                    <!-- Sun rays (hand-drawn style) -->
                    <g stroke="#FFD700" stroke-width="2" fill="none" filter="url(#sketch)" opacity="0.8">
                        <path d="M40,5 L40,15" stroke-dasharray="1,1"/>
                        <path d="M65,15 L60,20" stroke-dasharray="1,1"/>
                        <path d="M75,40 L65,40" stroke-dasharray="1,1"/>
                        <path d="M65,65 L60,60" stroke-dasharray="1,1"/>
                        <path d="M40,75 L40,65" stroke-dasharray="1,1"/>
                        <path d="M15,65 L20,60" stroke-dasharray="1,1"/>
                        <path d="M5,40 L15,40" stroke-dasharray="1,1"/>
                        <path d="M15,15 L20,20" stroke-dasharray="1,1"/>
                    </g>
                    <!-- Sun body -->
                    <circle cx="40" cy="40" r="18" fill="#FFD700" stroke="#FFA500" stroke-width="2" filter="url(#sketch)" opacity="0.9">
                        <animate attributeName="r" values="18;20;18" dur="3s" repeatCount="indefinite"/>
                    </circle>
                    <!-- Sun face -->
                    <circle cx="35" cy="35" r="2" fill="#FFA500"/>
                    <circle cx="45" cy="35" r="2" fill="#FFA500"/>
                    <path d="M32,45 Q40,50 48,45" stroke="#FFA500" stroke-width="1.5" fill="none"/>
                </svg>
            </div>
            """;
    }
    
    private String generateHanddrawnClouds() {
        return """
            <div class="handdrawn-clouds" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; z-index: 2; pointer-events: none;">
                <svg width="100%" height="100%">
                    <defs>
                        <filter id="cloudSketch">
                            <feTurbulence baseFrequency="0.1" numOctaves="2" result="noise"/>
                            <feDisplacementMap in="SourceGraphic" in2="noise" scale="2"/>
                        </filter>
                    </defs>
                    <!-- Handdrawn clouds -->
                    <g fill="rgba(255,255,255,0.8)" stroke="#87CEEB" stroke-width="1" filter="url(#cloudSketch)">
                        <ellipse cx="100" cy="60" rx="40" ry="20" opacity="0.7">
                            <animateTransform attributeName="transform" type="translate" values="0,0; 50,0; 0,0" dur="20s" repeatCount="indefinite"/>
                        </ellipse>
                        <ellipse cx="80" cy="50" rx="25" ry="15" opacity="0.6">
                            <animateTransform attributeName="transform" type="translate" values="0,0; 30,0; 0,0" dur="25s" repeatCount="indefinite"/>
                        </ellipse>
                        <ellipse cx="120" cy="70" rx="30" ry="18" opacity="0.8">
                            <animateTransform attributeName="transform" type="translate" values="0,0; 40,0; 0,0" dur="18s" repeatCount="indefinite"/>
                        </ellipse>
                    </g>
                </svg>
            </div>
            """;
    }
    
    private String generateHanddrawnThunderstorm() {
        return """
            <div class="handdrawn-storm" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; z-index: 2; pointer-events: none;">
                <svg width="100%" height="100%">
                    <defs>
                        <filter id="lightning">
                            <feTurbulence baseFrequency="0.5" numOctaves="1" result="noise"/>
                            <feDisplacementMap in="SourceGraphic" in2="noise" scale="3"/>
                        </filter>
                    </defs>
                    <!-- Lightning bolt (hand-drawn) -->
                    <path d="M150,50 L140,100 L155,100 L145,150 L165,100 L150,100 Z" 
                          fill="#FFFF00" 
                          stroke="#FFD700" 
                          stroke-width="1" 
                          filter="url(#lightning)" 
                          opacity="0">
                        <animate attributeName="opacity" values="0;1;0;1;0" dur="0.3s" repeatCount="indefinite" begin="0s"/>
                    </path>
                </svg>
            </div>
            """;
    }
    
    private String generateHanddrawnFog() {
        return """
            <div class="handdrawn-fog" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; z-index: 2; pointer-events: none;">
                <svg width="100%" height="100%">
                    <defs>
                        <filter id="foggy">
                            <feGaussianBlur stdDeviation="3"/>
                            <feTurbulence baseFrequency="0.02" numOctaves="1" result="noise"/>
                            <feDisplacementMap in="SourceGraphic" in2="noise" scale="5"/>
                        </filter>
                    </defs>
                    <!-- Fog wisps -->
                    <g fill="rgba(255,255,255,0.4)" filter="url(#foggy)">
                        <ellipse cx="50" cy="150" rx="80" ry="20" opacity="0.6">
                            <animateTransform attributeName="transform" type="translate" values="0,0; 20,0; 0,0" dur="15s" repeatCount="indefinite"/>
                        </ellipse>
                        <ellipse cx="200" cy="180" rx="100" ry="25" opacity="0.5">
                            <animateTransform attributeName="transform" type="translate" values="0,0; -15,0; 0,0" dur="18s" repeatCount="indefinite"/>
                        </ellipse>
                        <ellipse cx="300" cy="160" rx="70" ry="18" opacity="0.7">
                            <animateTransform attributeName="transform" type="translate" values="0,0; 25,0; 0,0" dur="12s" repeatCount="indefinite"/>
                        </ellipse>
                    </g>
                </svg>
            </div>
            """;
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
    
    // Data classes for handdrawn assets
    public static class HanddrawnAsset {
        private final String name;
        private final String imagePath;
        private final String description;
        
        public HanddrawnAsset(String name, String imagePath, String description) {
            this.name = name;
            this.imagePath = imagePath;
            this.description = description;
        }
        
        public String getName() { return name; }
        public String getImagePath() { return imagePath; }
        public String getDescription() { return description; }
    }
    
    public static class HanddrawnWeatherAnimation {
        private final String name;
        private final String animationPath;
        private final String emoji;
        private final String color;
        private final String description;
        
        public HanddrawnWeatherAnimation(String name, String animationPath, String emoji, String color, String description) {
            this.name = name;
            this.animationPath = animationPath;
            this.emoji = emoji;
            this.color = color;
            this.description = description;
        }
        
        public String getName() { return name; }
        public String getAnimationPath() { return animationPath; }
        public String getEmoji() { return emoji; }
        public String getColor() { return color; }
        public String getDescription() { return description; }
    }
    
    public List<String> getArtisticStyles() {
        return Arrays.asList(
            "Watercolor", "Ink Drawing", "Pencil Sketch", "Charcoal", 
            "Brush Painting", "Ink Wash", "Colored Pencil", "Pastel"
        );
    }
}