package se.campusmolndal.easyweather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class EasyWeatherApplication {
    public static void main(String[] args) {
        SpringApplication.run(EasyWeatherApplication.class, args);
    }
}
