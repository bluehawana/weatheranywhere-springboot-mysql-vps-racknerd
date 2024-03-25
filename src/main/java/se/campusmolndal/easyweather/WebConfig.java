package se.campusmolndal.easyweather;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import se.campusmolndal.easyweather.database.DatabaseHandler;

import javax.sql.DataSource;
import java.sql.DriverManager;

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
        dataSource.setUrl("jdbc:mysql://bluehawana.mysql.eu-west-1.rds.aliyuncs.com:3306/aliweather?useSSL=false&allowPublicKeyRetrieval=true");
        dataSource.setUsername("bluehawana");
        dataSource.setPassword("alybaba981020A!!");
        return dataSource;
    }

    @Bean
    public DatabaseHandler databaseHandler(DataSource dataSource) {
        return new DatabaseHandler(dataSource);
    }
}

