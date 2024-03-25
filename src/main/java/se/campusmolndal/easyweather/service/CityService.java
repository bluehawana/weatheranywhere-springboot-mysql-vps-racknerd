package se.campusmolndal.easyweather.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import se.campusmolndal.easyweather.models.City;

@Service
public class CityService {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CityService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public City getCityFromDatabase(String cityName) {
        String sql = "SELECT name, latitude, longitude FROM cities WHERE name = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{cityName}, (rs, rowNum) ->
                    new City(
                            rs.getString("name"),
                            rs.getDouble("latitude"),
                            rs.getDouble("longitude")
                    )
            );
        } catch (Exception e) {
            return null;
        }
    }

}