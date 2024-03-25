package se.campusmolndal.easyweather.service;

import java.net.http.HttpRequest;

public class WebClient {
    public WebClient() {
    }

    public String getWeather(String city) {
        return "Weather in " + city + " is 20 degrees";
    }

    public HttpRequest get() {
        return null;
    }

    public class Builder {
        public WebClient build() {
            return new WebClient();
        }
    }
}
