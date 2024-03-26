// WeatherAPIClient.java
package se.campusmolndal.easyweather.controllers;

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
        City city = cityService.getCityFromDatabase(cityName);

        if (city == null) {
            city = fetchAndSaveCityData(cityName);
            if (city == null) {
                throw new RuntimeException("Failed to fetch city data for " + cityName);
            }
        }

        double latitude = city.getLatitude();
        double longitude = city.getLongitude();
        URL apiUrl = null;
        try {
            apiUrl = new URL(API_BASE_URL + "?latitude=" + latitude + "&longitude=" + longitude + "&hourly=temperature_2m,weather_code,wind_speed_10m&wind_speed_unit=ms&forecast_days=1");
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

                City city = new City(cityName, latitude, longitude);
                DatabaseHandler databaseHandler = new DatabaseHandler(dataSource);
                databaseHandler.saveCity(cityName, latitude, longitude);
                return city;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    WeatherInfo createConnection(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to fetch weather data. Response code: " + responseCode);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        JSONObject jsonResponse = new JSONObject(response.toString());
        return parseWeatherData(jsonResponse);
    }

    public String buildHtml(WeatherInfo weatherInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div>");
        sb.append("<h2>").append(CityManager.getCity("SomeCityName")).append("</h2>");
        sb.append("<p>Temperature: ").append(weatherInfo.getTemperature()).append("°C</p>");
        sb.append("<p>Wind Speed: ").append(weatherInfo.getWindSpeed()).append(" m/s</p>");
        sb.append("<p>Description: ").append(weatherInfo.getDescription()).append("</p>");
        sb.append("</div>");
        return sb.toString();
    }

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
            return WEATHER_CODES.getOrDefault(weatherCode, "Unknown weather code");
        }
    }
    WeatherInfo parseWeatherData(JSONObject jsonResponse) {
        JSONObject hourlyData = jsonResponse.getJSONObject("hourly");
        double temperature = hourlyData.getJSONArray("temperature_2m").getDouble(0);
        double windSpeed = hourlyData.getJSONArray("wind_speed_10m").getDouble(0);
        int weatherCode = hourlyData.getJSONArray("weather_code").getInt(0);

        String description = WeatherDescription.getWeatherDescription(weatherCode);

        return new WeatherInfo(temperature, windSpeed, description);
    }
}
