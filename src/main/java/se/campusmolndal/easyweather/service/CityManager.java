package se.campusmolndal.easyweather.service;

import se.campusmolndal.easyweather.models.City;

import java.util.HashMap;
import java.util.Map;

public class CityManager {
    public static char[] getCity;
    private static Map<String, City> cities = new HashMap<>();

    public CityManager() {
        cities.put("Stockholm", new City("Stockholm", 59.3293, 18.0686));
        cities.put("Göteborg", new City("Göteborg", 57.7089, 11.9746));
        cities.put("Malmö", new City("Malmö", 55.6044, 13.0038));
        cities.put("Kiruna", new City("Kiruna", 67.8557, 20.2255));
        cities.put("Växjö", new City("Växjö", 56.8777, 14.8094));
        cities.put("Mölndal", new City("Mölndal", 57.6584, 12.0022));
        addCityAlias("Göteborg", "Götet");
        addCityAlias("Malmö", "Möllan");
        addCityAlias("Kiruna", "Kiran");
        addCityAlias("Växjö", "Växjöv");
        addCityAlias("Mölndal", "Mölndalv");
    }

    public static City getCity(String cityName) {
        // Check if the city name is an alias, if so, get the actual city name
        if (cities.containsKey(cityName)) {
            return cities.get(cityName);
        } else {
            // Iterate through city aliases to find the actual city name
            for (Map.Entry<String, City> entry : cities.entrySet()) {
                if (entry.getValue().getAliases().contains(cityName)) {
                    return entry.getValue();
                }
            }
        }
        // If the city name or alias is not found, return null
        return null;
    }

    public void addCityAlias(String cityName, String alias) {
        if (cities.containsKey(cityName)) {
            cities.get(cityName).addAlias(alias);
        }
    }
}
