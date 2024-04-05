package se.campusmolndal.easyweather.dsve.db;

import com.mysql.cj.exceptions.CJCommunicationsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class DatabaseConnection {
    private static String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    private String DB_SERVER;
    private static String DB_USER;
    private static String DB_PASSWORD;
    private String DB_PORT;
    private String DB_DATABASE;
    private static String DB_URL;

    @Autowired
    private Environment env;

    @PostConstruct
    public void init() {
        DB_SERVER = env.getProperty("DB_SERVER");
        DB_USER = env.getProperty("DB_USER");
        DB_PASSWORD = env.getProperty("DB_PASSWORD");
        DB_PORT = env.getProperty("DB_PORT");
        DB_DATABASE = env.getProperty("DB_DATABASE");
        DB_URL = "jdbc:mysql://" + DB_SERVER + ":" + DB_PORT + "/" + DB_DATABASE + "?useSSL=false&serverTimezone=UTC";

    }

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            System.out.println("Failed to create a connection to the database");
            if (e.getCause() instanceof CJCommunicationsException) {
                System.out.println("Unable to communicate with the database. Please check your network connection and database server status.");
                e.printStackTrace(); // Print the full stack trace for further debugging
            } else {
                e.printStackTrace(); // Print the full stack trace for further debugging
            }
            return null;
        }
    }
}