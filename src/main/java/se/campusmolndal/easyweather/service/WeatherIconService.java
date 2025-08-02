package se.campusmolndal.easyweather.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class WeatherIconService {
    
    // Streamline HQ icon mappings for weather conditions
    private static final Map<Integer, WeatherIcon> WEATHER_ICONS = new HashMap<>();
    
    static {
        // Clear/Sunny conditions
        WEATHER_ICONS.put(0, new WeatherIcon("sunny", "☀️", "https://cdn.streamlinehq.com/icons/weather/sun.svg"));
        WEATHER_ICONS.put(1, new WeatherIcon("partly-cloudy", "⛅", "https://cdn.streamlinehq.com/icons/weather/partly-cloudy.svg"));
        
        // Cloudy conditions
        WEATHER_ICONS.put(2, new WeatherIcon("cloudy", "☁️", "https://cdn.streamlinehq.com/icons/weather/cloud.svg"));
        WEATHER_ICONS.put(3, new WeatherIcon("overcast", "☁️", "https://cdn.streamlinehq.com/icons/weather/clouds.svg"));
        
        // Fog/Mist
        WEATHER_ICONS.put(45, new WeatherIcon("fog", "🌫️", "https://cdn.streamlinehq.com/icons/weather/fog.svg"));
        WEATHER_ICONS.put(48, new WeatherIcon("fog", "🌫️", "https://cdn.streamlinehq.com/icons/weather/fog.svg"));
        
        // Drizzle
        WEATHER_ICONS.put(51, new WeatherIcon("drizzle", "🌦️", "https://cdn.streamlinehq.com/icons/weather/drizzle.svg"));
        WEATHER_ICONS.put(53, new WeatherIcon("drizzle", "🌦️", "https://cdn.streamlinehq.com/icons/weather/drizzle.svg"));
        WEATHER_ICONS.put(55, new WeatherIcon("drizzle", "🌦️", "https://cdn.streamlinehq.com/icons/weather/drizzle.svg"));
        
        // Rain
        WEATHER_ICONS.put(61, new WeatherIcon("rain", "🌧️", "https://cdn.streamlinehq.com/icons/weather/rain.svg"));
        WEATHER_ICONS.put(63, new WeatherIcon("rain", "🌧️", "https://cdn.streamlinehq.com/icons/weather/rain.svg"));
        WEATHER_ICONS.put(65, new WeatherIcon("heavy-rain", "🌧️", "https://cdn.streamlinehq.com/icons/weather/heavy-rain.svg"));
        
        // Snow
        WEATHER_ICONS.put(71, new WeatherIcon("snow", "❄️", "https://cdn.streamlinehq.com/icons/weather/snow.svg"));
        WEATHER_ICONS.put(73, new WeatherIcon("snow", "❄️", "https://cdn.streamlinehq.com/icons/weather/snow.svg"));
        WEATHER_ICONS.put(75, new WeatherIcon("heavy-snow", "❄️", "https://cdn.streamlinehq.com/icons/weather/heavy-snow.svg"));
        
        // Thunderstorm
        WEATHER_ICONS.put(95, new WeatherIcon("thunderstorm", "⛈️", "https://cdn.streamlinehq.com/icons/weather/thunderstorm.svg"));
        WEATHER_ICONS.put(96, new WeatherIcon("thunderstorm", "⛈️", "https://cdn.streamlinehq.com/icons/weather/thunderstorm.svg"));
        WEATHER_ICONS.put(99, new WeatherIcon("thunderstorm", "⛈️", "https://cdn.streamlinehq.com/icons/weather/thunderstorm.svg"));
        
        // Wind
        WEATHER_ICONS.put(80, new WeatherIcon("windy", "💨", "https://cdn.streamlinehq.com/icons/weather/wind.svg"));
        WEATHER_ICONS.put(81, new WeatherIcon("windy", "💨", "https://cdn.streamlinehq.com/icons/weather/wind.svg"));
        WEATHER_ICONS.put(82, new WeatherIcon("windy", "💨", "https://cdn.streamlinehq.com/icons/weather/wind.svg"));
    }
    
    public WeatherIcon getWeatherIcon(int weatherCode) {
        return WEATHER_ICONS.getOrDefault(weatherCode, 
            new WeatherIcon("unknown", "❓", "https://cdn.streamlinehq.com/icons/weather/unknown.svg"));
    }
    
    public String getWeatherDescription(int weatherCode) {
        WeatherIcon icon = getWeatherIcon(weatherCode);
        return switch (weatherCode) {
            case 0 -> "Clear sky";
            case 1 -> "Mainly clear";
            case 2 -> "Partly cloudy";
            case 3 -> "Overcast";
            case 45, 48 -> "Fog";
            case 51, 53, 55 -> "Drizzle";
            case 61, 63 -> "Rain";
            case 65 -> "Heavy rain";
            case 71, 73 -> "Snow";
            case 75 -> "Heavy snow";
            case 80, 81, 82 -> "Rain showers";
            case 95, 96, 99 -> "Thunderstorm";
            default -> "Unknown weather";
        };
    }
    
    public static class WeatherIcon {
        private final String name;
        private final String emoji;
        private final String svgUrl;
        
        public WeatherIcon(String name, String emoji, String svgUrl) {
            this.name = name;
            this.emoji = emoji;
            this.svgUrl = svgUrl;
        }
        
        public String getName() { return name; }
        public String getEmoji() { return emoji; }
        public String getSvgUrl() { return svgUrl; }
        
        // Generate CSS class for styling
        public String getCssClass() {
            return "weather-icon-" + name.toLowerCase().replace("-", "_");
        }
        
        // Generate inline SVG icon (fallback if CDN fails)
        public String getInlineSvg() {
            return switch (name) {
                case "sunny" -> "<svg viewBox='0 0 24 24' fill='#FFD700'><circle cx='12' cy='12' r='5'/><path d='M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42'/></svg>";
                case "cloudy" -> "<svg viewBox='0 0 24 24' fill='#87CEEB'><path d='M18 10h-1.26A8 8 0 1 0 9 20h9a5 5 0 0 0 0-10z'/></svg>";
                case "rain" -> "<svg viewBox='0 0 24 24' fill='#4682B4'><path d='M18 10h-1.26A8 8 0 1 0 9 20h9a5 5 0 0 0 0-10z'/><path d='M8 19v2M8 13v2M16 19v2M16 13v2M12 21v2M12 15v2'/></svg>";
                case "snow" -> "<svg viewBox='0 0 24 24' fill='#E6E6FA'><path d='M18 10h-1.26A8 8 0 1 0 9 20h9a5 5 0 0 0 0-10z'/><circle cx='8' cy='16' r='1'/><circle cx='8' cy='20' r='1'/><circle cx='16' cy='16' r='1'/><circle cx='16' cy='20' r='1'/><circle cx='12' cy='18' r='1'/></svg>";
                case "windy" -> "<svg viewBox='0 0 24 24' fill='#B0C4DE'><path d='M9.59 4.59A2 2 0 1 1 11 8H2m10.59 11.41A2 2 0 1 0 14 16H2m15.73-8.27A2.5 2.5 0 1 1 19.5 12H2'/></svg>";
                default -> "<svg viewBox='0 0 24 24' fill='#999'><circle cx='12' cy='12' r='10'/><path d='M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3M12 17h.01'/></svg>";
            };
        }
    }
}