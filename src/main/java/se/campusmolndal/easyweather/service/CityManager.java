package se.campusmolndal.easyweather.service;

import se.campusmolndal.easyweather.models.City;
import se.campusmolndal.easyweather.database.DatabaseHandler;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CityManager {
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
        if (cities.containsKey(cityName)) {
            return cities.get(cityName);
        } else {
            for (Map.Entry<String, City> entry : cities.entrySet()) {
                if (entry.getValue().getAliases().contains(cityName)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    public void addCityAlias(String cityName, String alias) {
        if (cities.containsKey(cityName)) {
            cities.get(cityName).addAlias(alias);
        }
    }

    public City getCityFromDatabaseOrAddNew(String input) throws SQLException, IOException {
        City city = CityManager.getCity(input);

        if (city == null) {
            DataSource dataSource = getDataSource();
            DatabaseHandler databaseHandler = new DatabaseHandler(dataSource);
            city = databaseHandler.getCityFromDatabase(input);

            if (city == null) {
                System.out.println("Staden finns inte i databasen. Vill du lägga till den? (ja/nej)");
                Scanner scanner = new Scanner(System.in);
                String response = scanner.nextLine();
                if (response.equalsIgnoreCase("ja")) {
                    double[] latLong = fetchLatLong(input);
                    if (latLong != null) {
                        city = new City(input, latLong[0], latLong[1]);
                        CityManager.cities.put(city.getName(), city);
                        databaseHandler.saveCity(input, city.getLatitude(), city.getLongitude());
                    } else {
                        System.out.println("Kunde inte hitta latitud och longitud för staden.");
                    }
                }
            }
        }

        return city;
    }
    private DataSource getDataSource() {
        return null;
    }

    private double[] fetchLatLong(String input) {
        return null;
    }
}