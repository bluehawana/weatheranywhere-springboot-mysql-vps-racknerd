package se.campusmolndal.easyweather;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;
import se.campusmolndal.easyweather.database.DatabaseHandler;
import se.campusmolndal.easyweather.models.City;

import javax.sql.DataSource;
import java.util.List;


@Configuration
public class WebConfig {

    @Value("${spring.datasource.url}")
    private String dataSourceUrl;

    @Value("${spring.datasource.username}")
    private String dataSourceUsername;

    @Value("${spring.datasource.password}")
    private String dataSourcePassword;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(dataSourceUrl);
        dataSource.setUsername(dataSourceUsername);
        dataSource.setPassword(dataSourcePassword);
        return dataSource;
    }

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



    @Bean
    public DatabaseHandler databaseHandler(DataSource dataSource) {
        return new DatabaseHandler(dataSource);
    }
}
