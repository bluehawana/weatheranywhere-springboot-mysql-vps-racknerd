package se.campusmolndal.easyweather.models;

import java.util.Collection;

public class City{
    private final String name;
    private final double latitude;
    private final double longitude;
    private double temperature;
    private double windSpeed;
    private String description;

    public City(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public City(String city, String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public City(String city) {
        this.name = city;
        this.latitude = getLatitude();
        this.longitude = getLongitude();
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
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

    public Collection<Object> getAliases() {
        addCityAlias("Göteborg", "Götet");
        addCityAlias("Malmö", "Möllan");
        addCityAlias("Kiruna", "Kiran");
        addCityAlias("Växjö", "Växjöv");
        addCityAlias("Mölndal", "Mölndalv");
        addCityAlias("Stockholm", "Storstan");
        addCityAlias("Växjö", "Växjöv");

        return null;
    }

    private void addCityAlias(String växjö, String växjöv) {
    }

    public void addAlias(String alias) {
    }
}

