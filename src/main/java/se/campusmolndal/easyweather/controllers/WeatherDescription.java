// WeatherDescription.java
package se.campusmolndal.easyweather.controllers;

import java.util.HashMap;
import java.util.Map;

public class WeatherDescription {
    private static final Map<Integer, String> WEATHER_CODES = new HashMap<>();

    static {
        WEATHER_CODES.put(0, "Clear sky");
        WEATHER_CODES.put(1, "Mainly clear");
        WEATHER_CODES.put(2, "Partly cloudy");
        WEATHER_CODES.put(3, "Overcast");
        WEATHER_CODES.put(45, "Fog");
        WEATHER_CODES.put(48, "Depositing rime fog");
        WEATHER_CODES.put(51, "Drizzle: Light");
        WEATHER_CODES.put(53, "Drizzle: Moderate");
        WEATHER_CODES.put(55, "Drizzle: Dense intensity");
        WEATHER_CODES.put(56, "Freezing Drizzle: Light");
        WEATHER_CODES.put(57, "Freezing Drizzle: Dense intensity");
        WEATHER_CODES.put(61, "Rain: Slight");
        WEATHER_CODES.put(63, "Rain: Moderate");
        WEATHER_CODES.put(65, "Rain: Heavy intensity");
        WEATHER_CODES.put(66, "Freezing Rain: Light");
        WEATHER_CODES.put(67, "Freezing Rain: Heavy intensity");
        WEATHER_CODES.put(71, "Snow fall: Slight");
        WEATHER_CODES.put(73, "Snow fall: Moderate");
        WEATHER_CODES.put(75, "Snow fall: Heavy intensity");
        WEATHER_CODES.put(77, "Snow grains");
        WEATHER_CODES.put(80, "Rain showers: Slight");
        WEATHER_CODES.put(81, "Rain showers: Moderate");
        WEATHER_CODES.put(82, "Rain showers: Violent");
        WEATHER_CODES.put(85, "Snow showers: Slight");
        WEATHER_CODES.put(86, "Snow showers: Heavy");
        WEATHER_CODES.put(95, "Thunderstorm: Slight or moderate");
        WEATHER_CODES.put(96, "Thunderstorm with slight hail");
        WEATHER_CODES.put(99, "Thunderstorm with heavy hail");
    }

    public static String getWeatherDescription(int weatherCode) {
        return WEATHER_CODES.get(weatherCode);
    }
}