package se.campusmolndal.easyweather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@EnableConfigurationProperties
@PropertySource("classpath:application.properties") // Load properties from application.properties
public class EasyWeatherApplication {
    public static void main(String[] args) {
        SpringApplication.run(EasyWeatherApplication.class, args);
    }
}
