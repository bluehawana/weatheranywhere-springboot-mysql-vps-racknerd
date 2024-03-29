package se.campusmolndal.easyweather.CityRepository;

import se.campusmolndal.easyweather.models.City;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CityRepository {
    private final JdbcTemplate jdbcTemplate;

    public CityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<City> findDistinctCities() {
        String sql = "SELECT DISTINCT city, lat, lng FROM aliweather";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new City(
                rs.getString("city"),
                rs.getDouble("lat"),
                rs.getDouble("lng")
        ));
    }
}
