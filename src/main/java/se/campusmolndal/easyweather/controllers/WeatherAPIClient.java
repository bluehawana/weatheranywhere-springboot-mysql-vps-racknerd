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

    @Value("${opencage.api.key}")
    private String opencageApiKey;

    @Autowired
    private DataSource dataSource;

    public WeatherAPIClient(CityService cityService) {
        this.cityService = cityService;
    }

    public WeatherInfo fetchWeather(String cityName) {
        City city = fetchAndSaveCityData(cityName);
        if (city == null) {
            throw new RuntimeException("Failed to fetch city data for " + cityName);
        }

        double latitude = city.getLatitude();
        double longitude = city.getLongitude();
        URL apiUrl = null;
        try {
            apiUrl = new URL(API_BASE_URL + "?latitude=" + city.getLatitude() + "&longitude=" + city.getLongitude() + "&current=temperature_2m,weather_code,wind_speed_10m"+"&timezone=auto");
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            try {
                return createConnection(connection);
            } finally {
                connection.disconnect();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch weather data", e);
        }
    }

    private City fetchAndSaveCityData(String cityName) {
        try {
            DatabaseHandler databaseHandler = new DatabaseHandler(dataSource);
            if (databaseHandler.cityExists(cityName)) {
                return databaseHandler.getCityFromDatabase(cityName);
            }

            URL url = new URL(OPENCAGE_GEOCODING_API_URL + "?q=" + URLEncoder.encode(cityName, "UTF-8") + "&key=" + opencageApiKey);
            HttpURLConnection geocodingConnection = (HttpURLConnection) url.openConnection();
            geocodingConnection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(geocodingConnection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            geocodingConnection.disconnect();

            JSONObject geocodingResponse = new JSONObject(response.toString());
            if (geocodingResponse.getJSONArray("results").length() > 0) {
                JSONObject result = geocodingResponse.getJSONArray("results").getJSONObject(0);
                double latitude = result.getJSONObject("geometry").getDouble("lat");
                double longitude = result.getJSONObject("geometry").getDouble("lng");

                City city = databaseHandler.saveCity(cityName, latitude, longitude);
                return city;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            // Log or handle the SQLException appropriately
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

        // Create WeatherInfo object
        WeatherInfo weatherInfo = new WeatherInfo(temperature, windSpeed, weatherCode);

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