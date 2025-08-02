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

    @Value("${noun.project.api.key:}")
    private String nounProjectApiKey;
    
    @Value("${noun.project.api.secret:}")
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
        
        // Fallback to city-specific emoji if Noun Project fails
        return getCityEmoji(cityName);
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
            return getWeatherEmoji("clear");
        }
        
        String iconTerm = getOptimalWeatherTerm(weatherDescription);
        String result = getNounProjectIcon(iconTerm, 64);
        
        // If specific weather term fails, try the original description
        if (result.contains("📍")) {
            result = getNounProjectIcon(weatherDescription, 64);
        }
        
        // If Noun Project fails completely, use weather-appropriate emoji
        if (result.contains("📍")) {
            return getWeatherEmoji(iconTerm);
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
            // Use the correct Noun Project v2 API endpoint for icon search
            String baseUrl = "https://api.thenounproject.com/v2/icon";
            String query = "query=" + URLEncoder.encode(term, StandardCharsets.UTF_8) + "&limit=1&thumbnail_size=" + size;
            String url = baseUrl + "?" + query;
            
            System.out.println("Fetching icon for term: " + term);
            System.out.println("API URL: " + url);
            
            // Generate OAuth1 signature
            String authHeader = generateOAuth1Header("GET", baseUrl, query);
            
            if (authHeader.isEmpty()) {
                System.err.println("Failed to generate OAuth header - API keys might be missing");
                return getFallbackIcon(term, size);
            }
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", authHeader)
                    .header("User-Agent", "WeatherApp/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("Response code: " + response.statusCode());
            
            if (response.statusCode() == 200) {
                String body = response.body();
                System.out.println("Response body preview: " + body.substring(0, Math.min(200, body.length())));
                
                // Parse JSON response to extract icon URL
                String iconUrl = extractIconUrlFromJson(body);
                if (iconUrl != null && !iconUrl.isEmpty()) {
                    System.out.println("Found icon URL: " + iconUrl);
                    return String.format("<img src='%s' alt='%s' style='width: %dpx; height: %dpx;' />", 
                                       iconUrl, term, size, size);
                }
            } else if (response.statusCode() == 401) {
                System.err.println("Noun Project API authentication failed - check your API keys");
            } else {
                System.err.println("Noun Project API error: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch icon for: " + term + " - " + e.getMessage());
            e.printStackTrace();
        }
        
        return getFallbackIcon(term, size);
    }
    
    private String generateOAuth1Header(String method, String baseUrl, String parameters) {
        try {
            // OAuth1 parameters - simplified for request token flow
            String nonce = generateNonce();
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            
            // Collect all parameters for sorting
            java.util.Map<String, String> allParams = new java.util.TreeMap<>();
            
            // Add query parameters - decode them first, then re-encode properly
            String[] paramPairs = parameters.split("&");
            for (String param : paramPairs) {
                if (!param.isEmpty()) {
                    String[] keyValue = param.split("=", 2);
                    if (keyValue.length == 2) {
                        // Store decoded values, we'll encode them later
                        allParams.put(keyValue[0], java.net.URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8));
                    }
                }
            }
            
            // Add OAuth parameters
            allParams.put("oauth_consumer_key", nounProjectApiKey);
            allParams.put("oauth_nonce", nonce);
            allParams.put("oauth_signature_method", "HMAC-SHA1");
            allParams.put("oauth_timestamp", timestamp);
            allParams.put("oauth_version", "1.0");
            
            // Build sorted parameter string for signature
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
            
            // Create signing key - for request token (no oauth_token_secret)
            String signingKey = URLEncoder.encode(nounProjectApiSecret, StandardCharsets.UTF_8) + "&";
            
            // Generate signature
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secretKey = new SecretKeySpec(signingKey.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
            mac.init(secretKey);
            byte[] signature = mac.doFinal(signatureBaseString.getBytes(StandardCharsets.UTF_8));
            String encodedSignature = Base64.getEncoder().encodeToString(signature);
            
            // Build Authorization header - simpler format matching RestSharp
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
    
    private String extractIconUrlFromJson(String jsonResponse) {
        // Try different possible JSON fields for icon URLs
        String[] possibleFields = {"icon_url", "thumbnail_url", "preview_url", "svg_url"};
        
        for (String field : possibleFields) {
            String fieldPattern = "\"" + field + "\":\"";
            int start = jsonResponse.indexOf(fieldPattern);
            if (start > 0) {
                start += fieldPattern.length();
                int end = jsonResponse.indexOf("\"", start);
                if (end > start) {
                    String url = jsonResponse.substring(start, end);
                    // Unescape JSON string
                    url = url.replace("\\/", "/");
                    return url;
                }
            }
        }
        
        // Also try to extract from icons array
        int iconsStart = jsonResponse.indexOf("\"icons\":[");
        if (iconsStart > 0) {
            int firstIcon = jsonResponse.indexOf("{", iconsStart);
            if (firstIcon > 0) {
                int iconEnd = jsonResponse.indexOf("}", firstIcon);
                if (iconEnd > firstIcon) {
                    String iconObj = jsonResponse.substring(firstIcon, iconEnd + 1);
                    return extractIconUrlFromJson(iconObj);
                }
            }
        }
        
        return null;
    }
    
    private String getFallbackIcon(String term, int size) {
        // Return appropriate fallback icon based on term type
        if (isWeatherTerm(term)) {
            return getWeatherEmoji(term);
        } else {
            // For city landmarks, return the hardcoded fallback
            return String.format("<span style='font-size: %dpx;'>📍 %s</span>", size/2, term);
        }
    }
    
    private boolean isWeatherTerm(String term) {
        String lowerTerm = term.toLowerCase();
        return lowerTerm.contains("sun") || lowerTerm.contains("rain") || lowerTerm.contains("cloud") || 
               lowerTerm.contains("snow") || lowerTerm.contains("storm") || lowerTerm.contains("wind") ||
               lowerTerm.contains("fog") || lowerTerm.contains("clear") || lowerTerm.contains("weather");
    }
    
    private String getWeatherEmoji(String weatherTerm) {
        return switch (weatherTerm.toLowerCase()) {
            case "thunderstorm", "lightning" -> "⛈️";
            case "heavy rain", "downpour" -> "🌧️";
            case "light rain", "drizzle" -> "🌦️";
            case "rain", "shower" -> "🌧️";
            case "heavy snow", "blizzard" -> "❄️";
            case "light snow", "snow" -> "❄️";
            case "sleet" -> "🌨️";
            case "sun", "sunny", "clear" -> "☀️";
            case "partly cloudy", "partly" -> "⛅";
            case "overcast", "mostly cloudy" -> "☁️";
            case "cloud", "cloudy" -> "☁️";
            case "fog", "mist" -> "🌫️";
            case "haze", "hazy" -> "🌫️";
            case "wind", "windy" -> "💨";
            case "hot weather", "heat" -> "🌡️";
            case "cold weather", "freezing" -> "🥶";
            case "tornado" -> "🌪️";
            case "hurricane" -> "🌀";
            case "hail" -> "🧊";
            default -> "🌤️";
        };
    }
    
    private String getCityEmoji(String cityName) {
        return switch (cityName.toLowerCase()) {
            case "london" -> "🇬🇧";
            case "paris" -> "🇫🇷";
            case "tokyo" -> "🇯🇵";
            case "new york", "newyork" -> "🗽";
            case "los angeles" -> "🎬";
            case "sydney" -> "🇦🇺";
            case "beijing" -> "🇨🇳";
            case "shanghai" -> "🏙️";
            case "hong kong" -> "🏙️";
            case "dubai" -> "🕌";
            case "mumbai" -> "🇮🇳";
            case "moscow" -> "🇷🇺";
            case "rome" -> "🏛️";
            case "athens" -> "🏛️";
            case "cairo" -> "🏺";
            case "rio de janeiro", "rio" -> "🇧🇷";
            case "barcelona" -> "🇪🇸";
            case "amsterdam" -> "🇳🇱";
            case "berlin" -> "🇩🇪";
            case "istanbul" -> "🇹🇷";
            default -> "🏙️";
        };
    }
}