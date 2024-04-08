package se.campusmolndal.easyweather.database;

import se.campusmolndal.easyweather.service.CityManager;
import se.campusmolndal.easyweather.models.City;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;

public class DatabaseHandler {
    private final DataSource dataSource;

    public DatabaseHandler(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static void printSQLException(SQLException ex) {
        if (ex != null) {
            System.out.println("Error Message: " + ex.getMessage());
            System.out.println("Error Code: " + ex.getErrorCode());
            System.out.println("SQL State: " + ex.getSQLState());
            Throwable t = ex.getCause();
            while (t != null) {
                System.out.println("Cause: " + t);
                t = t.getCause();
            }
        }
    }

    public boolean cityExists(String cityName) {
        String checkCitySql = "SELECT COUNT(*) FROM aliweather WHERE cityName = ?";
        try (Connection conn = dataSource.getConnection()) {
            conn.setCatalog("aliweather"); // Set the database name
            try (PreparedStatement checkCityStmt = conn.prepareStatement(checkCitySql)) {
                checkCityStmt.setString(1, cityName);
                ResultSet rs = checkCityStmt.executeQuery();
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return false;
    }

    public City getCityFromDatabase(String cityName) throws SQLException, IOException {
        String input = cityName;
        City city = CityManager.getCity(input);

        String getCitySql = "SELECT * FROM aliweather WHERE cityName = ?";
        try (Connection conn = dataSource.getConnection()) {
            conn.setCatalog("aliweather"); // Set the database name
            try (PreparedStatement getCityStmt = conn.prepareStatement(getCitySql)) {
                getCityStmt.setString(1, cityName);
                ResultSet rs = getCityStmt.executeQuery();
                if (rs.next()) {
                    String name = rs.getString("cityName");
                    double latitude = rs.getDouble("latitude");
                    double longitude = rs.getDouble("longitude");
                    System.out.println("Retrieved city: " + name);
                    return new City(name, latitude, longitude);
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        System.out.println("Could not retrieve city: " + cityName);
        return null;
    }

    //We need to avoid SQL injection attacks, so we use a prepared statement
    public City saveCity(String cityName, double latitude, double longitude) throws SQLException, IOException {
        String checkCitySql = "SELECT COUNT(*) FROM aliweather WHERE cityName = ?";
        String insertCitySql = "INSERT INTO aliweather (cityName, latitude, longitude) VALUES (?, ?, ?)";

        try (Connection conn = dataSource.getConnection()) {
            conn.setCatalog("aliweather"); // Set the database name

            try (PreparedStatement checkCityStmt = conn.prepareStatement(checkCitySql)) {
                checkCityStmt.setString(1, cityName);
                ResultSet rs = checkCityStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    // City already exists, return the weather inquiry for the city
                    return getCityFromDatabase(cityName);
                }
            }

            try (PreparedStatement insertCityStmt = conn.prepareStatement(insertCitySql)) {
                insertCityStmt.setString(1, cityName);
                insertCityStmt.setDouble(2, latitude);
                insertCityStmt.setDouble(3, longitude);
                insertCityStmt.executeUpdate();
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        // If the city is newly inserted, return the weather inquiry for the city
        return getCityFromDatabase(cityName);
    }
}