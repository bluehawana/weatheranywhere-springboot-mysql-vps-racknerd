package se.campusmolndal.easyweather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@EnableConfigurationProperties
@ComponentScan({"se.campusmolndal.easyweather.controllers", "se.campusmolndal.easyweather.service",}) // Scan for components in the se.campusmolndal.easyweather package
@PropertySource("classpath:application.properties") // Load properties from application.properties
public class EasyWeatherApplication {
    public static void main(String[] args) {
        SpringApplication.run(EasyWeatherApplication.class, args);
    }
}
