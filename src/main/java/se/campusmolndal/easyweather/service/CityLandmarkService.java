package se.campusmolndal.easyweather.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.util.Base64;
import java.security.SecureRandom;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
public class CityLandmarkService {

    @Value("${noun.project.api.key:008adfb4f566422e8d88d5d74bdbe3e7}")
    private String nounProjectApiKey;
    
    @Value("${noun.project.api.secret:db9c192c25834a82b770d2a43227363c}")
    private String nounProjectApiSecret;
    
    @Autowired
    private AIWeatherService aiWeatherService;
    
    private final HttpClient httpClient;

    public CityLandmarkService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String getCityIcon(String cityName) {
        // First try Noun Project API if available
        if (nounProjectApiKey != null && !nounProjectApiKey.isEmpty()) {
            String iconTerm = getOptimalCityTerm(cityName);
            String result = getNounProjectIcon(iconTerm, 64);
            
            if (!result.contains("📍")) {
                return result;
            }
        }
        
        // Fallback to simple text if Noun Project fails
        return String.format("<span style='font-size: %dpx;'>🏛️ %s</span>", 32, cityName);
    }
    
    private String getOptimalCityTerm(String cityName) {
        return switch (cityName.toLowerCase()) {
            case "london" -> "big ben";
            case "paris" -> "eiffel tower";
            case "tokyo" -> "tokyo tower";
            case "new york", "newyork" -> "statue of liberty";
            case "los angeles" -> "hollywood sign";
            case "sydney" -> "opera house";
            case "beijing" -> "forbidden city";
            case "shanghai" -> "oriental pearl tower";
            case "hong kong" -> "hong kong skyline";
            case "dubai" -> "burj khalifa";
            case "mumbai" -> "gateway of india";
            case "moscow" -> "red square";
            case "rome" -> "colosseum";
            case "athens" -> "parthenon";
            case "cairo" -> "pyramid";
            case "rio de janeiro", "rio" -> "christ redeemer";
            case "barcelona" -> "sagrada familia";
            case "amsterdam" -> "windmill";
            case "berlin" -> "brandenburg gate";
            case "istanbul" -> "hagia sophia";
            default -> cityName + " landmark";
        };
    }

    public String getWeatherIcon(String weatherDescription) {
        if (weatherDescription == null || weatherDescription.isEmpty()) {
            return getNounProjectIcon("weather", 64);
        }
        
        String iconTerm = getOptimalWeatherTerm(weatherDescription);
        String result = getNounProjectIcon(iconTerm, 64);
        
        // If specific weather term fails, try the original description
        if (result.contains("📍")) {
            result = getNounProjectIcon(weatherDescription, 64);
        }
        
        // If original description fails, try "weather"
        if (result.contains("📍")) {
            result = getNounProjectIcon("weather", 64);
        }
        
        return result;
    }
    
    private String getOptimalWeatherTerm(String weatherDescription) {
        String lowercaseDesc = weatherDescription.toLowerCase();
        
        // Specific weather condition mapping
        if (lowercaseDesc.contains("thunderstorm") || lowercaseDesc.contains("thunder")) {
            return "thunderstorm";
        } else if (lowercaseDesc.contains("lightning")) {
            return "lightning";
        } else if (lowercaseDesc.contains("hail")) {
            return "hail";
        } else if (lowercaseDesc.contains("tornado")) {
            return "tornado";
        } else if (lowercaseDesc.contains("hurricane")) {
            return "hurricane";
        } else if (lowercaseDesc.contains("blizzard")) {
            return "blizzard";
        } else if (lowercaseDesc.contains("heavy rain") || lowercaseDesc.contains("downpour")) {
            return "heavy rain";
        } else if (lowercaseDesc.contains("light rain") || lowercaseDesc.contains("drizzle")) {
            return "light rain";
        } else if (lowercaseDesc.contains("rain") || lowercaseDesc.contains("shower")) {
            return "rain";
        } else if (lowercaseDesc.contains("heavy snow")) {
            return "heavy snow";
        } else if (lowercaseDesc.contains("light snow")) {
            return "light snow";
        } else if (lowercaseDesc.contains("snow")) {
            return "snow";
        } else if (lowercaseDesc.contains("sleet")) {
            return "sleet";
        } else if (lowercaseDesc.contains("freezing")) {
            return "freezing";
        } else if (lowercaseDesc.contains("clear") || lowercaseDesc.contains("sunny")) {
            return "sun";
        } else if (lowercaseDesc.contains("partly cloudy") || lowercaseDesc.contains("partly")) {
            return "partly cloudy";
        } else if (lowercaseDesc.contains("mostly cloudy") || lowercaseDesc.contains("overcast")) {
            return "overcast";
        } else if (lowercaseDesc.contains("cloud")) {
            return "cloud";
        } else if (lowercaseDesc.contains("fog") || lowercaseDesc.contains("mist")) {
            return "fog";
        } else if (lowercaseDesc.contains("haze") || lowercaseDesc.contains("hazy")) {
            return "haze";
        } else if (lowercaseDesc.contains("windy") || lowercaseDesc.contains("wind")) {
            return "wind";
        } else if (lowercaseDesc.contains("hot") || lowercaseDesc.contains("heat")) {
            return "hot weather";
        } else if (lowercaseDesc.contains("cold") || lowercaseDesc.contains("freezing")) {
            return "cold weather";
        } else {
            // Default fallback - try the original description
            return weatherDescription;
        }
    }

    public String getNounProjectIcon(String term, int size) {
        try {
            String baseUrl = "https://api.thenounproject.com/v2/icon";
            String query = "query=" + URLEncoder.encode(term, StandardCharsets.UTF_8) + "&limit=1&styles=line,solid";
            String url = baseUrl + "?" + query;
            
            // Generate OAuth1 signature
            String authHeader = generateOAuth1Header("GET", baseUrl, query);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", authHeader)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                // Simple parsing - in real implementation would use JSON parser
                String body = response.body();
                if (body.contains("\"icon_url\"")) {
                    String iconUrl = extractIconUrl(body);
                    if (iconUrl != null) {
                        return String.format("<img src='%s' alt='%s' style='width: %dpx; height: %dpx;' />", 
                                           iconUrl, term, size, size);
                    }
                }
            } else {
                System.err.println("Noun Project API error: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch icon for: " + term + " - " + e.getMessage());
        }
        
        // Fallback to simple text
        return String.format("<span style='font-size: %dpx;'>📍 %s</span>", size/2, term);
    }
    
    private String generateOAuth1Header(String method, String baseUrl, String parameters) {
        try {
            // OAuth1 parameters
            String nonce = generateNonce();
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            
            // Collect all parameters for sorting
            java.util.Map<String, String> allParams = new java.util.TreeMap<>();
            
            // Add query parameters
            String[] paramPairs = parameters.split("&");
            for (String param : paramPairs) {
                if (!param.isEmpty()) {
                    String[] keyValue = param.split("=", 2);
                    if (keyValue.length == 2) {
                        allParams.put(keyValue[0], keyValue[1]);
                    }
                }
            }
            
            // Add OAuth parameters
            allParams.put("oauth_consumer_key", nounProjectApiKey);
            allParams.put("oauth_nonce", nonce);
            allParams.put("oauth_signature_method", "HMAC-SHA1");
            allParams.put("oauth_timestamp", timestamp);
            allParams.put("oauth_version", "1.0");
            
            // Build sorted parameter string
            StringBuilder sortedParams = new StringBuilder();
            for (java.util.Map.Entry<String, String> entry : allParams.entrySet()) {
                if (sortedParams.length() > 0) sortedParams.append("&");
                sortedParams.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                          .append("=")
                          .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }
            
            // Create signature base string
            String signatureBaseString = method + "&" + 
                URLEncoder.encode(baseUrl, StandardCharsets.UTF_8) + "&" + 
                URLEncoder.encode(sortedParams.toString(), StandardCharsets.UTF_8);
            
            // Create signing key (consumer secret + "&" + token secret, but we have no token secret)
            String signingKey = URLEncoder.encode(nounProjectApiSecret, StandardCharsets.UTF_8) + "&";
            
            // Generate signature
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secretKey = new SecretKeySpec(signingKey.getBytes(), "HmacSHA1");
            mac.init(secretKey);
            byte[] signature = mac.doFinal(signatureBaseString.getBytes());
            String encodedSignature = Base64.getEncoder().encodeToString(signature);
            
            // Build Authorization header (don't double-encode the signature)
            return String.format("OAuth oauth_consumer_key=\"%s\", oauth_nonce=\"%s\", oauth_signature=\"%s\", oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"%s\", oauth_version=\"1.0\"",
                nounProjectApiKey, nonce, encodedSignature, timestamp);
                
        } catch (Exception e) {
            System.err.println("OAuth1 signature generation failed: " + e.getMessage());
            return "";
        }
    }
    
    private String generateNonce() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes).replaceAll("[^a-zA-Z0-9]", "");
    }

    private String extractIconUrl(String jsonResponse) {
        // Simple string extraction - would use proper JSON parser in production
        int start = jsonResponse.indexOf("\"icon_url\":\"");
        if (start > 0) {
            start += 12; // length of "icon_url":"
            int end = jsonResponse.indexOf("\"", start);
            if (end > start) {
                return jsonResponse.substring(start, end);
            }
        }
        return null;
    }
}