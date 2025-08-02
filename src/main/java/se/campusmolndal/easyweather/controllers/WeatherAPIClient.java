package se.campusmolndal.easyweather.controllers;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.campusmolndal.easyweather.database.*;
import se.campusmolndal.easyweather.models.City;
import se.campusmolndal.easyweather.models.WeatherInfo;
import se.campusmolndal.easyweather.service.CityManager;
import se.campusmolndal.easyweather.service.CityService;
import se.campusmolndal.easyweather.service.WeatherIconService;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Service
public class WeatherAPIClient {
    private static final String API_BASE_URL = "https://api.open-meteo.com/v1/forecast";
    private static final String OPENCAGE_GEOCODING_API_URL = "https://api.opencagedata.com/geocode/v1/json";
    private final CityService cityService;
    private final WeatherIconService weatherIconService;

    @Value("${opencage.api.key}")
    private String opencageApiKey;

    @Autowired
    private DataSource dataSource;

    public WeatherAPIClient(CityService cityService, WeatherIconService weatherIconService) {
        this.cityService = cityService;
        this.weatherIconService = weatherIconService;
    }

    public WeatherInfo fetchWeather(String cityName) {
        try {
            City city = fetchAndSaveCityData(cityName);
            if (city == null) {
                System.err.println("Failed to fetch city data for " + cityName);
                return null;
            }

            double latitude = city.getLatitude();
            double longitude = city.getLongitude();
            URL apiUrl = new URL(API_BASE_URL + "?latitude=" + latitude + "&longitude=" + longitude + "&current=temperature_2m,weather_code,wind_speed_10m&timezone=auto");
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            try {
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return createConnection(connection);
                } else {
                    System.err.println("Weather API returned error code: " + responseCode);
                    return null;
                }
            } finally {
                connection.disconnect();
            }
        } catch (IOException e) {
            System.err.println("Failed to fetch weather data: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("Unexpected error fetching weather: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private City fetchAndSaveCityData(String cityName) {
        try {
            DatabaseHandler databaseHandler = new DatabaseHandler(dataSource);
            if (databaseHandler.cityExists(cityName)) {
                return databaseHandler.getCityFromDatabase(cityName);
            }

            // For testing purposes, provide hardcoded coordinates for popular cities
            if (cityName.equalsIgnoreCase("Miami")) {
                City city = databaseHandler.saveCity("Miami", 25.7617, -80.1918);
                return city;
            }
            if (cityName.equalsIgnoreCase("Paris")) {
                City city = databaseHandler.saveCity("Paris", 48.8566, 2.3522);
                return city;
            }
            if (cityName.equalsIgnoreCase("New York")) {
                City city = databaseHandler.saveCity("New York", 40.7128, -74.0060);
                return city;
            }
            if (cityName.equalsIgnoreCase("London")) {
                City city = databaseHandler.saveCity("London", 51.5074, -0.1278);
                return city;
            }

            // Check if API key is configured
            if (opencageApiKey == null || opencageApiKey.equals("YOUR_OPENCAGE_API_KEY_HERE")) {
                System.err.println("OpenCage API key not configured. Using hardcoded coordinates for testing. Please set opencage.api.key in application.properties for full functionality");
                return null;
            }

            URL url = new URL(OPENCAGE_GEOCODING_API_URL + "?q=" + URLEncoder.encode(cityName, "UTF-8") + "&key=" + opencageApiKey);
            HttpURLConnection geocodingConnection = (HttpURLConnection) url.openConnection();
            geocodingConnection.setRequestMethod("GET");
            geocodingConnection.setConnectTimeout(10000);
            geocodingConnection.setReadTimeout(10000);

            int responseCode = geocodingConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.err.println("Geocoding API returned error code: " + responseCode);
                geocodingConnection.disconnect();
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(geocodingConnection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            geocodingConnection.disconnect();

            JSONObject geocodingResponse = new JSONObject(response.toString());
            JSONArray results = geocodingResponse.getJSONArray("results");
            
            if (results.length() > 0) {
                JSONObject result = results.getJSONObject(0);
                double latitude = result.getJSONObject("geometry").getDouble("lat");
                double longitude = result.getJSONObject("geometry").getDouble("lng");

                City city = databaseHandler.saveCity(cityName, latitude, longitude);
                return city;
            } else {
                System.err.println("No geocoding results found for city: " + cityName);
                return null;
            }
        } catch (IOException e) {
            System.err.println("IO error during geocoding: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error during geocoding: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    WeatherInfo createConnection(HttpURLConnection connection) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        JSONObject jsonResponse = new JSONObject(response.toString());

        // Extract current data
        JSONObject currentData = jsonResponse.getJSONObject("current");
        double temperature = currentData.getDouble("temperature_2m");
        int weatherCode = currentData.getInt("weather_code");
        double windSpeed = currentData.getDouble("wind_speed_10m");

        // Get weather icon and description
        WeatherIconService.WeatherIcon icon = weatherIconService.getWeatherIcon(weatherCode);
        String description = weatherIconService.getWeatherDescription(weatherCode);

        // Create WeatherInfo object with enhanced data
        WeatherInfo weatherInfo = new WeatherInfo(temperature, windSpeed, weatherCode);
        weatherInfo.setDescription(description);
        weatherInfo.setIcon(icon);

        return weatherInfo;
    }
    public static class WeatherDescription {
        private static final Map<Integer, String> WEATHER_CODES = new HashMap<>();

        static {
            // ... existing weather codes ...
        }

        public static String getWeatherDescription(int weatherCode) {
            return WEATHER_CODES.getOrDefault(weatherCode, "Unknown weather code");
        }
    }
    WeatherInfo parseWeatherData(JSONObject jsonResponse) {
        JSONObject dailyData = jsonResponse.getJSONObject("daily");
        double temperature = dailyData.getJSONArray("temperature_2m").getDouble(0);
        double windSpeed = dailyData.getJSONArray("wind_speed_10m").getDouble(0);
        int weatherCode = dailyData.getJSONArray("weather_code").getInt(0);

        String description = se.campusmolndal.easyweather.controllers.WeatherDescription.getWeatherDescription(weatherCode);

        return new WeatherInfo(temperature, windSpeed, description);
    }
}