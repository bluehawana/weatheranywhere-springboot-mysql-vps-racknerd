package se.campusmolndal.easyweather.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            // Create cities table if it doesn't exist
            String createTableSql = """
                CREATE TABLE IF NOT EXISTS cities (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    cityName VARCHAR(255) NOT NULL UNIQUE,
                    latitude DOUBLE NOT NULL,
                    longitude DOUBLE NOT NULL
                )
                """;
            
            jdbcTemplate.execute(createTableSql);
            System.out.println("✅ Cities table created/verified successfully");
            
            // Check if table has any data
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cities", Integer.class);
            System.out.println("📊 Cities table currently has " + count + " entries");
            
            // Pre-populate with popular cities if table is empty
            if (count == 0) {
                System.out.println("🌍 Pre-populating database with popular cities...");
                populatePopularCities();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}