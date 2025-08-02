package se.campusmolndal.easyweather.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.campusmolndal.easyweather.models.WeatherInfo;

import java.util.HashMap;
import java.util.Map;

@Service
public class LandmarkAnimationService {

    @Autowired
    private AIWeatherService aiWeatherService;

    private static final Map<String, String> CITY_LANDMARKS = new HashMap<>();
    
    static {
        // European cities
        CITY_LANDMARKS.put("stockholm", "Royal Palace and Gamla Stan");
        CITY_LANDMARKS.put("göteborg", "Göteborgs Operan and harbor");
        CITY_LANDMARKS.put("gothenburg", "Göteborgs Operan and harbor");
        CITY_LANDMARKS.put("malmö", "Turning Torso skyscraper");
        CITY_LANDMARKS.put("london", "Big Ben and Tower Bridge");
        CITY_LANDMARKS.put("paris", "Eiffel Tower");
        CITY_LANDMARKS.put("berlin", "Brandenburg Gate");
        
        // Asian cities
        CITY_LANDMARKS.put("tokyo", "Tokyo Tower and Mount Fuji");
        CITY_LANDMARKS.put("shanghai", "Oriental Pearl Tower and Bund");
        CITY_LANDMARKS.put("beijing", "Forbidden City and Great Wall");
        CITY_LANDMARKS.put("mohe", "Northern lights viewing tower");
        
        // American cities
        CITY_LANDMARKS.put("new york", "Statue of Liberty and Empire State Building");
        CITY_LANDMARKS.put("san francisco", "Golden Gate Bridge");
        CITY_LANDMARKS.put("chicago", "Willis Tower");
    }

    public String generateLandmarkAnimation(String city, WeatherInfo weatherInfo) {
        String landmark = CITY_LANDMARKS.getOrDefault(city.toLowerCase(), "city skyline");
        String weatherCondition = weatherInfo.getDescription().toLowerCase();
        
        return generateSVGAnimation(city, landmark, weatherCondition, weatherInfo);
    }

    private String generateSVGAnimation(String city, String landmark, String weather, WeatherInfo weatherInfo) {
        StringBuilder svg = new StringBuilder();
        
        svg.append("<div class='landmark-container'>")
           .append("<svg width='400' height='300' viewBox='0 0 400 300' class='landmark-svg'>")
           .append("<defs>")
           .append(generateWeatherEffects(weather))
           .append("</defs>");
        
        // Generate city-specific landmark
        svg.append(generateLandmarkSVG(city.toLowerCase(), landmark));
        
        // Add weather effects
        svg.append(generateWeatherAnimations(weather, weatherInfo));
        
        svg.append("</svg>")
           .append("<div class='landmark-info'>")
           .append("<h3>").append(landmark).append("</h3>")
           .append("<p>").append(city).append(" • ").append(weatherInfo.getDescription()).append("</p>")
           .append("</div>")
           .append("</div>");
        
        return svg.toString();
    }

    private String generateLandmarkSVG(String city, String landmark) {
        switch (city) {
            case "stockholm":
                return generateStockholmPalace();
            case "göteborg":
            case "gothenburg":
                return generateGothenburgOpera();
            case "london":
                return generateBigBen();
            case "tokyo":
                return generateTokyoTower();
            case "shanghai":
                return generateOrientalPearl();
            case "mohe":
                return generateMoheTower();
            default:
                return generateGenericSkyline();
        }
    }

    private String generateStockholmPalace() {
        return """
            <!-- Stockholm Royal Palace -->
            <rect x="150" y="180" width="100" height="80" fill="#D4AF37" stroke="#B8860B" stroke-width="2"/>
            <rect x="160" y="160" width="80" height="20" fill="#CD853F"/>
            <polygon points="150,180 200,140 250,180" fill="#8B4513"/>
            <rect x="170" y="200" width="15" height="25" fill="#654321"/>
            <rect x="215" y="200" width="15" height="25" fill="#654321"/>
            <circle cx="200" cy="150" r="8" fill="#FFD700"/>
            <text x="200" y="280" text-anchor="middle" fill="#333" font-size="12">Royal Palace</text>
            """;
    }

    private String generateGothenburgOpera() {
        return """
            <!-- Gothenburg Opera House -->
            <ellipse cx="200" cy="200" rx="80" ry="40" fill="#4682B4" opacity="0.8"/>
            <path d="M 120 200 Q 200 160 280 200 Q 200 240 120 200" fill="#87CEEB"/>
            <rect x="180" y="180" width="40" height="40" fill="#2F4F4F" opacity="0.7"/>
            <circle cx="200" cy="200" r="15" fill="#FFD700" opacity="0.6"/>
            <text x="200" y="280" text-anchor="middle" fill="#333" font-size="12">Göteborgs Operan</text>
            <animateTransform attributeName="transform" type="rotate" values="0 200 200;2 200 200;0 200 200" dur="4s" repeatCount="indefinite"/>
            """;
    }

    private String generateBigBen() {
        return """
            <!-- Big Ben -->
            <rect x="190" y="100" width="20" height="120" fill="#8B4513"/>
            <rect x="185" y="90" width="30" height="20" fill="#654321"/>
            <polygon points="185,90 200,70 215,90" fill="#2F4F4F"/>
            <circle cx="200" cy="130" r="12" fill="#F5F5DC" stroke="#333" stroke-width="2"/>
            <line x1="200" y1="130" x2="200" y2="120" stroke="#333" stroke-width="2"/>
            <line x1="200" y1="130" x2="208" y2="130" stroke="#333" stroke-width="1"/>
            <text x="200" y="280" text-anchor="middle" fill="#333" font-size="12">Big Ben</text>
            """;
    }

    private String generateTokyoTower() {
        return """
            <!-- Tokyo Tower -->
            <polygon points="200,80 180,220 220,220" fill="#FF4500" stroke="#DC143C" stroke-width="2"/>
            <rect x="195" y="100" width="10" height="20" fill="#DC143C"/>
            <rect x="195" y="140" width="10" height="20" fill="#DC143C"/>
            <rect x="195" y="180" width="10" height="20" fill="#DC143C"/>
            <circle cx="200" cy="85" r="5" fill="#FFD700"/>
            <text x="200" y="280" text-anchor="middle" fill="#333" font-size="12">Tokyo Tower</text>
            <animateTransform attributeName="transform" type="scale" values="1;1.02;1" dur="3s" repeatCount="indefinite"/>
            """;
    }

    private String generateOrientalPearl() {
        return """
            <!-- Oriental Pearl Tower -->
            <line x1="200" y1="80" x2="200" y2="220" stroke="#C0C0C0" stroke-width="8"/>
            <circle cx="200" cy="100" r="15" fill="#FF69B4" opacity="0.8"/>
            <circle cx="200" cy="140" r="20" fill="#FF1493" opacity="0.8"/>
            <circle cx="200" cy="190" r="25" fill="#DC143C" opacity="0.8"/>
            <text x="200" y="280" text-anchor="middle" fill="#333" font-size="12">Oriental Pearl</text>
            <animateTransform attributeName="transform" type="rotate" values="0 200 150;360 200 150" dur="10s" repeatCount="indefinite"/>
            """;
    }

    private String generateMoheTower() {
        return """
            <!-- Mohe Northern Lights Tower -->
            <rect x="190" y="150" width="20" height="70" fill="#4682B4"/>
            <polygon points="190,150 200,120 210,150" fill="#2F4F4F"/>
            <rect x="185" y="180" width="30" height="15" fill="#696969"/>
            <circle cx="200" cy="135" r="8" fill="#00FFFF" opacity="0.7">
                <animate attributeName="opacity" values="0.3;1;0.3" dur="2s" repeatCount="indefinite"/>
            </circle>
            <text x="200" y="280" text-anchor="middle" fill="#333" font-size="12">Aurora Tower</text>
            """;
    }

    private String generateGenericSkyline() {
        return """
            <!-- Generic City Skyline -->
            <rect x="120" y="180" width="30" height="40" fill="#696969"/>
            <rect x="160" y="160" width="25" height="60" fill="#778899"/>
            <rect x="195" y="140" width="35" height="80" fill="#2F4F4F"/>
            <rect x="240" y="170" width="28" height="50" fill="#696969"/>
            <rect x="275" y="155" width="32" height="65" fill="#778899"/>
            <text x="200" y="280" text-anchor="middle" fill="#333" font-size="12">City Skyline</text>
            """;
    }

    private String generateWeatherEffects(String weather) {
        StringBuilder effects = new StringBuilder();
        
        if (weather.contains("rain")) {
            effects.append("""
                <pattern id="rainPattern" patternUnits="userSpaceOnUse" width="4" height="20">
                    <line x1="2" y1="0" x2="2" y2="20" stroke="#4169E1" stroke-width="1" opacity="0.6">
                        <animateTransform attributeName="transform" type="translateY" values="0;20;0" dur="0.5s" repeatCount="indefinite"/>
                    </line>
                </pattern>
                """);
        }
        
        if (weather.contains("snow")) {
            effects.append("""
                <pattern id="snowPattern" patternUnits="userSpaceOnUse" width="20" height="20">
                    <circle cx="5" cy="5" r="2" fill="white" opacity="0.8">
                        <animateTransform attributeName="transform" type="translateY" values="0;20;0" dur="3s" repeatCount="indefinite"/>
                    </circle>
                    <circle cx="15" cy="15" r="1.5" fill="white" opacity="0.6">
                        <animateTransform attributeName="transform" type="translateY" values="0;20;0" dur="4s" repeatCount="indefinite"/>
                    </circle>
                </pattern>
                """);
        }
        
        if (weather.contains("clear") || weather.contains("sunny")) {
            effects.append("""
                <radialGradient id="sunGlow" cx="50%" cy="30%">
                    <stop offset="0%" style="stop-color:#FFD700;stop-opacity:0.8"/>
                    <stop offset="100%" style="stop-color:#FFA500;stop-opacity:0.2"/>
                </radialGradient>
                """);
        }
        
        return effects.toString();
    }

    private String generateWeatherAnimations(String weather, WeatherInfo weatherInfo) {
        StringBuilder animations = new StringBuilder();
        
        if (weather.contains("rain")) {
            animations.append("""
                <rect x="0" y="0" width="400" height="300" fill="url(#rainPattern)" opacity="0.4"/>
                <circle cx="50" cy="50" r="20" fill="#708090" opacity="0.6">
                    <animate attributeName="r" values="15;25;15" dur="4s" repeatCount="indefinite"/>
                </circle>
                <circle cx="350" cy="80" r="25" fill="#708090" opacity="0.5">
                    <animate attributeName="r" values="20;30;20" dur="5s" repeatCount="indefinite"/>
                </circle>
                """);
        }
        
        if (weather.contains("snow")) {
            animations.append("""
                <rect x="0" y="0" width="400" height="300" fill="url(#snowPattern)" opacity="0.6"/>
                """);
        }
        
        if (weather.contains("clear") || weather.contains("sunny")) {
            animations.append("""
                <circle cx="350" cy="50" r="30" fill="url(#sunGlow)">
                    <animate attributeName="opacity" values="0.8;1;0.8" dur="3s" repeatCount="indefinite"/>
                </circle>
                <g transform="translate(350,50)">
                    <g>
                        <line x1="-45" y1="0" x2="-35" y2="0" stroke="#FFD700" stroke-width="2"/>
                        <line x1="35" y1="0" x2="45" y2="0" stroke="#FFD700" stroke-width="2"/>
                        <line x1="0" y1="-45" x2="0" y2="-35" stroke="#FFD700" stroke-width="2"/>
                        <line x1="0" y1="35" x2="0" y2="45" stroke="#FFD700" stroke-width="2"/>
                        <animateTransform attributeName="transform" type="rotate" values="0;360" dur="20s" repeatCount="indefinite"/>
                    </g>
                </g>
                """);
        }
        
        if (weather.contains("cloud") || weather.contains("overcast")) {
            animations.append("""
                <ellipse cx="100" cy="60" rx="40" ry="20" fill="#B0C4DE" opacity="0.7">
                    <animateTransform attributeName="transform" type="translateX" values="0;50;0" dur="8s" repeatCount="indefinite"/>
                </ellipse>
                <ellipse cx="300" cy="80" rx="35" ry="18" fill="#D3D3D3" opacity="0.6">
                    <animateTransform attributeName="transform" type="translateX" values="0;-30;0" dur="10s" repeatCount="indefinite"/>
                </ellipse>
                """);
        }
        
        return animations.toString();
    }

    public String generateLandmarkCSS() {
        return """
            <style>
            .landmark-container {
                position: relative;
                display: inline-block;
                margin: 20px;
                background: linear-gradient(to bottom, #87CEEB 0%, #98FB98 100%);
                border-radius: 15px;
                padding: 20px;
                box-shadow: 0 8px 25px rgba(0,0,0,0.2);
                transition: transform 0.3s ease;
            }
            
            .landmark-container:hover {
                transform: translateY(-5px);
            }
            
            .landmark-svg {
                display: block;
                margin: 0 auto;
                background: linear-gradient(to bottom, #87CEEB 0%, #90EE90 100%);
                border-radius: 10px;
            }
            
            .landmark-info {
                text-align: center;
                margin-top: 15px;
                color: #333;
            }
            
            .landmark-info h3 {
                margin: 0 0 5px 0;
                font-size: 1.2em;
                font-weight: bold;
                color: #2F4F4F;
            }
            
            .landmark-info p {
                margin: 0;
                font-size: 0.9em;
                color: #666;
            }
            </style>
            """;
    }
}