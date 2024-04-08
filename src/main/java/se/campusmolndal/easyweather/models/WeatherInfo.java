package se.campusmolndal.easyweather.models;

import org.json.JSONObject;
import java.util.HashMap;
import se.campusmolndal.easyweather.controllers.WeatherDescription;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import static se.campusmolndal.easyweather.controllers.WeatherDescription.getWeatherDescription;

public class WeatherInfo {
    private final double temperature; // Temperaturen i grader Celsius
    private final double windSpeed; // Vindhastigheten i km/h
    private final String description; // Beskrivningen av vädret
    private final int weatherCode;

    public WeatherInfo(double temperature, double windSpeed, String description, int weatherCode) {
        this.temperature = temperature;
        this.windSpeed = windSpeed;
        this.description = description;
        this.weatherCode = weatherCode;
    }

    public WeatherInfo(double temperature, double windSpeed, int weatherCode) {
        this(temperature, windSpeed,  getWeatherDescription(weatherCode), weatherCode);
    }

    public WeatherInfo(double temperature, double windSpeed, int weatherCode, double dailyTemperature, int dailyWeatherCode, double dailyWindSpeed) {
        this.temperature = dailyTemperature;
        this.windSpeed = dailyWindSpeed;
        this.description = getWeatherDescription(weatherCode);
        this.weatherCode = dailyWeatherCode;
    }

    public WeatherInfo(double temperature, double windSpeed, String description) {
        this(temperature, windSpeed, description, 0);
    }

    public double getTemperature() {
        return temperature;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public String getDescription() {
        return description;
}
        //after we got weather description, we need swedish translation to show the user
    public String getSwedishDescription( )    {
        switch (description) {

            case "Clear sky":
                return "Klart himmel";
            case "Mainly clear":
                return "Huvudsakligen klart";
            case "Partly cloudy":
                return "Delvis molnigt";
            case "Overcast":
                return "Mulet";
            case "Fog":
                return "Dimma";
            case "Depositing rime fog":
                return "Deposition rim dimma";
            case "Drizzle: Light":
                return "Duggregn: Lätt";
            case "Drizzle: Moderate":
                return "Duggregn: Måttlig";
            case "Drizzle: Dense intensity":
                return "Duggregn: Tät intensitet";
            case "Freezing Drizzle: Light":
                return "Frysande duggregn: Lätt";
            case "Freezing Drizzle: Dense intensity":
                return "Frysande duggregn: Tät intensitet";
            case "Rain: Slight":
                return "Regn: Lätt";
            case "Rain: Moderate":
                return "Regn: Måttlig";
            case "Rain: Heavy intensity":
                return "Regn: Kraftig intensitet";
            case "Freezing Rain: Light":
                return "Frysande regn: Lätt";
            case "Freezing Rain: Heavy intensity":
                return "Frysande regn: Kraftig intensitet";
            case "Snow fall: Slight":
                return "Snöfall: Lätt";
            case "Snow fall: Moderate":
                return "Snöfall: Måttlig";
            case "Snow fall: Heavy intensity":
                return "Snöfall: Kraftig intensitet";
            case "Snow grains":
                return "Snökorn";
            case "Rain showers: Slight":
                return "Regnskurar: Lätta";
            case "Rain showers: Moderate":
                return "Regnskurar: Måttliga";
            case "Rain showers: Violent":
                return "Regnskurar: Våldsamma";
            case "Snow showers: Slight":
                return "Snöskurar: Lätta";
            case "Snow showers: Heavy":
                return "Snöskurar: Kraftiga";
            case "Thunderstorm: Slight or moderate":
                return "Åskväder: Lätt eller måttlig";
            case "Thunderstorm with slight hail":
                return "Åskväder med lätt hagel";
            case "Thunderstorm with heavy hail":
                return "Åskväder med kraftigt hagel";
            default:
                return "Okänt väder";

        }
    }

}