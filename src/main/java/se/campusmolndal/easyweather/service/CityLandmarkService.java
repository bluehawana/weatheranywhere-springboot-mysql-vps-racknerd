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
         // First try OpenAI-generated SVG landmarks with retries for important cities
        try {
            if (aiWeatherService != null) {
                // Create a dummy weather info for the SVG generation
                se.campusmolndal.easyweather.models.WeatherInfo dummyWeather = 
                    new se.campusmolndal.easyweather.models.WeatherInfo(20.0, 3.0, "clear", 0);
                
                // Try up to 2 times for major cities to get proper landmarks
                int maxAttempts = isMajorCity(cityName) ? 2 : 1;
                
                for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                    System.out.println("Attempting OpenAI landmark generation for " + cityName + " (attempt " + attempt + "/" + maxAttempts + ")");
                    
                    String aiSvg = aiWeatherService.generateAILandmarkSVG(cityName, dummyWeather);
                    if (aiSvg != null && aiSvg.contains("<svg")) {
                        System.out.println("Successfully generated OpenAI SVG for " + cityName);
                        return aiSvg;
                    }
                    
                    // Small delay between attempts for major cities
                    if (attempt < maxAttempts) {
                        Thread.sleep(1000);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("OpenAI SVG generation failed for " + cityName + ": " + e.getMessage());
        }
        
        // If OpenAI fails, try city-specific landmark SVGs before falling back to emojis
        String specificLandmark = getCitySpecificLandmarkSVG(cityName);
        if (specificLandmark != null) {
            return specificLandmark;
        }
        
        // Final fallback to city-specific emoji
        return getCityEmoji(cityName);
    }
    
    private String getOptimalCityTerm(String cityName) {
        // Use AI to determine the best landmark for unknown cities
        String aiLandmark = getAILandmarkSuggestion(cityName);
        if (aiLandmark != null && !aiLandmark.isEmpty()) {
            return aiLandmark;
        }
        
        // Fallback to predefined mappings for common cities
        return switch (cityName.toLowerCase()) {
            case "london" -> "big ben";
            case "paris" -> "eiffel tower";
            case "tokyo" -> "tokyo tower";
            case "new york", "newyork" -> "statue of liberty";
            case "los angeles" -> "hollywood sign";
            case "san francisco", "sanfrancisco", "sf" -> "golden gate bridge";
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
            case "gothenburg", "göteborg" -> "poseidon statue";
            case "stockholm" -> "city hall";
            case "copenhagen" -> "little mermaid";
            case "oslo" -> "opera house";
            case "helsinki" -> "cathedral";
            case "reykjavik" -> "hallgrimskirkja";
            default -> cityName + " landmark";
        };
    }
    
    private String getAILandmarkSuggestion(String cityName) {
        try {
            if (aiWeatherService == null) {
                return null;
            }
            
            String prompt = String.format(
                "What is the most famous landmark or monument in %s? " +
                "Respond with ONLY the landmark name in 2-3 words maximum. " +
                "Examples: 'eiffel tower', 'big ben', 'statue liberty', 'poseidon statue'. " +
                "No explanations, just the landmark name.", 
                cityName
            );
            
            String aiResponse = aiWeatherService.callOpenAIForLandmark(prompt);
            if (aiResponse != null && !aiResponse.trim().isEmpty()) {
                // Clean up the response - remove quotes, extra words
                aiResponse = aiResponse.toLowerCase()
                    .replaceAll("[\"']", "")
                    .replaceAll("\\b(the|a|an)\\b", "")
                    .replaceAll("\\s+", " ")
                    .trim();
                
                System.out.println("AI suggested landmark for " + cityName + ": " + aiResponse);
                return aiResponse;
            }
        } catch (Exception e) {
            System.err.println("AI landmark suggestion failed for " + cityName + ": " + e.getMessage());
        }
        
        return null;
    }

    public String getWeatherIcon(String weatherDescription) {
        if (weatherDescription == null || weatherDescription.isEmpty()) {
            return getASCIIWeatherIcon("clear");
        }
        
        // Use ASCII weather symbols instead of emojis or Noun Project icons
        return getASCIIWeatherIcon(weatherDescription);
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
            // For city landmarks, try to get city-specific emoji first
            // Extract city name from landmark term (e.g., "poseidon statue" -> "gothenburg")
            String cityName = extractCityFromLandmarkTerm(term);
            return getCityEmoji(cityName);
        }
    }
    
    private String extractCityFromLandmarkTerm(String landmarkTerm) {
        String term = landmarkTerm.toLowerCase();
        if (term.contains("poseidon")) return "gothenburg";
        if (term.contains("big ben")) return "london";
        if (term.contains("eiffel")) return "paris";
        if (term.contains("statue of liberty")) return "new york";
        if (term.contains("hollywood")) return "los angeles";
        if (term.contains("golden gate")) return "san francisco";
        if (term.contains("opera house")) return "sydney";
        if (term.contains("colosseum")) return "rome";
        if (term.contains("parthenon")) return "athens";
        if (term.contains("city hall") && term.contains("stockholm")) return "stockholm";
        if (term.contains("little mermaid")) return "copenhagen";
        if (term.contains("hallgrimskirkja")) return "reykjavik";
        
        // If it ends with " landmark", extract the city name
        if (term.endsWith(" landmark")) {
            return term.substring(0, term.length() - " landmark".length());
        }
        
        return term;
    }
    
    private boolean isWeatherTerm(String term) {
        String lowerTerm = term.toLowerCase();
        return lowerTerm.contains("sun") || lowerTerm.contains("rain") || lowerTerm.contains("cloud") || 
               lowerTerm.contains("snow") || lowerTerm.contains("storm") || lowerTerm.contains("wind") ||
               lowerTerm.contains("fog") || lowerTerm.contains("clear") || lowerTerm.contains("weather");
    }
    
    private String getASCIIWeatherIcon(String weatherDescription) {
        String desc = weatherDescription.toLowerCase();
        
        if (desc.contains("clear") || desc.contains("sunny")) {
            return "<pre style='display: inline-block; margin: 0; font-family: monospace; font-size: 14px;'>" +
                   "    \\   /    \n" +
                   "     .-.     \n" +
                   "  ‒ (   ) ‒  \n" +
                   "     `-'     \n" +
                   "    /   \\    </pre>";
        } else if (desc.contains("rain") || desc.contains("shower")) {
            return "<pre style='display: inline-block; margin: 0; font-family: monospace; font-size: 14px;'>" +
                   "     .-.     \n" +
                   "    (   ).   \n" +
                   "   (___(__)  \n" +
                   "    ' ' ' '  \n" +
                   "   ' ' ' '   </pre>";
        } else if (desc.contains("snow")) {
            return "<pre style='display: inline-block; margin: 0; font-family: monospace; font-size: 14px;'>" +
                   "     .-.     \n" +
                   "    (   ).   \n" +
                   "   (___(__)  \n" +
                   "    * * * *  \n" +
                   "   * * * *   </pre>";
        } else if (desc.contains("cloud")) {
            return "<pre style='display: inline-block; margin: 0; font-family: monospace; font-size: 14px;'>" +
                   "             \n" +
                   "     .--.    \n" +
                   "  .-(    ).  \n" +
                   " (___.__)__) \n" +
                   "             </pre>";
        } else if (desc.contains("wind")) {
            return "<pre style='display: inline-block; margin: 0; font-family: monospace; font-size: 14px;'>" +
                   "             \n" +
                   "  ~~~   ~~~ \n" +
                   " ~~~ ~~~ ~~~\n" +
                   "~~~ ~~~ ~~~ \n" +
                   "             </pre>";
        } else if (desc.contains("thunderstorm") || desc.contains("storm")) {
            return "<pre style='display: inline-block; margin: 0; font-family: monospace; font-size: 14px;'>" +
                   "     .-.     \n" +
                   "    (   ).   \n" +
                   "   (___(__)  \n" +
                   "    ⚡ ' ⚡   \n" +
                   "   ' ⚡ ' '   </pre>";
        } else if (desc.contains("fog") || desc.contains("mist")) {
            return "<pre style='display: inline-block; margin: 0; font-family: monospace; font-size: 14px;'>" +
                   "             \n" +
                   " _ - _ - _ - \n" +
                   "  _ - _ - _  \n" +
                   " _ - _ - _ - \n" +
                   "             </pre>";
        } else {
            // Default partly cloudy
            return "<pre style='display: inline-block; margin: 0; font-family: monospace; font-size: 14px;'>" +
                   "   \\  /      \n" +
                   " _ /\"\".-.    \n" +
                   "   \\_(   ).  \n" +
                   "   /(___(__)  \n" +
                   "             </pre>";
        }
    }
    
    public String getWeatherEmoji(String weatherTerm) {
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
    
    private boolean isMajorCity(String cityName) {
        String city = cityName.toLowerCase();
        return city.equals("beijing") || city.equals("london") || city.equals("paris") || 
               city.equals("tokyo") || city.equals("new york") || city.equals("newyork") ||
               city.equals("rome") || city.equals("moscow") || city.equals("berlin") ||
               city.equals("madrid") || city.equals("cairo") || city.equals("mumbai") ||
               city.equals("shanghai") || city.equals("sydney") || city.equals("los angeles");
    }
    
    private String getCitySpecificLandmarkSVG(String cityName) {
        return switch (cityName.toLowerCase()) {
            case "beijing" -> generateBeijingForbiddenCity();
            case "rome" -> generateRomeColosseum(); 
            case "athens" -> generateAthensParthenon();
            case "cairo" -> generateCairoPyramid();
            case "moscow" -> generateMoscowRedSquare();
            case "mumbai" -> generateMumbaiGateway();
            default -> null;
        };
    }
    
    private String generateBeijingForbiddenCity() {
        return """
            <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" style="width: 100px; height: 100px;">
                <!-- Main Palace Structure -->
                <rect x="15" y="60" width="70" height="25" fill="none" stroke="#000000" stroke-width="2"/>
                
                <!-- Traditional Chinese Roof -->
                <polygon points="10,60 50,45 90,60" fill="none" stroke="#000000" stroke-width="2"/>
                <polygon points="15,50 50,38 85,50" fill="none" stroke="#000000" stroke-width="1.5"/>
                
                <!-- Palace Gates and Columns -->
                <rect x="45" y="70" width="10" height="15" fill="none" stroke="#000000" stroke-width="2"/>
                <line x1="25" y1="60" x2="25" y2="85" stroke="#000000" stroke-width="2"/>
                <line x1="35" y1="60" x2="35" y2="85" stroke="#000000" stroke-width="2"/>
                <line x1="65" y1="60" x2="65" y2="85" stroke="#000000" stroke-width="2"/>
                <line x1="75" y1="60" x2="75" y2="85" stroke="#000000" stroke-width="2"/>
                
                <!-- Decorative Elements -->
                <line x1="10" y1="60" x2="90" y2="60" stroke="#000000" stroke-width="1"/>
                <circle cx="50" cy="52" r="2" fill="none" stroke="#000000" stroke-width="1"/>
                
                <text x="50" y="95" text-anchor="middle" font-size="6" fill="#000000">Forbidden City</text>
            </svg>
            """;
    }
    
    private String generateRomeColosseum() {
        return """
            <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" style="width: 100px; height: 100px;">
                <!-- Main Colosseum Structure -->
                <ellipse cx="50" cy="65" rx="35" ry="20" fill="none" stroke="#000000" stroke-width="2"/>
                <ellipse cx="50" cy="60" rx="35" ry="20" fill="none" stroke="#000000" stroke-width="2"/>
                
                <!-- Arches -->
                <rect x="20" y="55" width="6" height="15" fill="none" stroke="#000000" stroke-width="1"/>
                <rect x="30" y="55" width="6" height="15" fill="none" stroke="#000000" stroke-width="1"/>
                <rect x="40" y="55" width="6" height="15" fill="none" stroke="#000000" stroke-width="1"/>
                <rect x="54" y="55" width="6" height="15" fill="none" stroke="#000000" stroke-width="1"/>
                <rect x="64" y="55" width="6" height="15" fill="none" stroke="#000000" stroke-width="1"/>
                <rect x="74" y="55" width="6" height="15" fill="none" stroke="#000000" stroke-width="1"/>
                
                <!-- Upper Level Arches -->
                <rect x="25" y="45" width="4" height="10" fill="none" stroke="#000000" stroke-width="1"/>
                <rect x="35" y="45" width="4" height="10" fill="none" stroke="#000000" stroke-width="1"/>
                <rect x="45" y="45" width="4" height="10" fill="none" stroke="#000000" stroke-width="1"/>
                <rect x="56" y="45" width="4" height="10" fill="none" stroke="#000000" stroke-width="1"/>
                <rect x="66" y="45" width="4" height="10" fill="none" stroke="#000000" stroke-width="1"/>
                
                <text x="50" y="95" text-anchor="middle" font-size="6" fill="#000000">Colosseum</text>
            </svg>
            """;
    }
    
    private String generateAthensParthenon() {
        return """
            <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" style="width: 100px; height: 100px;">
                <!-- Temple Base -->
                <rect x="10" y="70" width="80" height="15" fill="none" stroke="#000000" stroke-width="2"/>
                
                <!-- Columns -->
                <line x1="15" y1="45" x2="15" y2="70" stroke="#000000" stroke-width="2"/>
                <line x1="25" y1="45" x2="25" y2="70" stroke="#000000" stroke-width="2"/>
                <line x1="35" y1="45" x2="35" y2="70" stroke="#000000" stroke-width="2"/>
                <line x1="45" y1="45" x2="45" y2="70" stroke="#000000" stroke-width="2"/>
                <line x1="55" y1="45" x2="55" y2="70" stroke="#000000" stroke-width="2"/>
                <line x1="65" y1="45" x2="65" y2="70" stroke="#000000" stroke-width="2"/>
                <line x1="75" y1="45" x2="75" y2="70" stroke="#000000" stroke-width="2"/>
                <line x1="85" y1="45" x2="85" y2="70" stroke="#000000" stroke-width="2"/>
                
                <!-- Pediment -->
                <polygon points="5,45 50,25 95,45" fill="none" stroke="#000000" stroke-width="2"/>
                <rect x="5" y="45" width="90" height="5" fill="none" stroke="#000000" stroke-width="1"/>
                
                <text x="50" y="95" text-anchor="middle" font-size="6" fill="#000000">Parthenon</text>
            </svg>
            """;
    }
    
    private String generateCairoPyramid() {
        return """
            <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" style="width: 100px; height: 100px;">
                <!-- Main Pyramid -->
                <polygon points="50,25 20,80 80,80" fill="none" stroke="#000000" stroke-width="2"/>
                
                <!-- Pyramid Details -->
                <line x1="50" y1="25" x2="65" y2="65" stroke="#000000" stroke-width="1"/>
                <line x1="50" y1="25" x2="35" y2="65" stroke="#000000" stroke-width="1"/>
                <line x1="25" y1="72" x2="75" y2="72" stroke="#000000" stroke-width="1"/>
                <line x1="30" y1="76" x2="70" y2="76" stroke="#000000" stroke-width="1"/>
                
                <!-- Small pyramid nearby -->
                <polygon points="75,55 68,75 82,75" fill="none" stroke="#000000" stroke-width="1"/>
                
                <!-- Ground line -->
                <line x1="15" y1="80" x2="85" y2="80" stroke="#000000" stroke-width="1"/>
                
                <text x="50" y="95" text-anchor="middle" font-size="6" fill="#000000">Great Pyramid</text>
            </svg>
            """;
    }
    
    private String generateMoscowRedSquare() {
        return """
            <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" style="width: 100px; height: 100px;">
                <!-- St. Basil's Cathedral domes -->
                <circle cx="50" cy="35" r="8" fill="none" stroke="#000000" stroke-width="2"/>
                <circle cx="35" cy="45" r="6" fill="none" stroke="#000000" stroke-width="1.5"/>
                <circle cx="65" cy="45" r="6" fill="none" stroke="#000000" stroke-width="1.5"/>
                <circle cx="40" cy="55" r="5" fill="none" stroke="#000000" stroke-width="1"/>
                <circle cx="60" cy="55" r="5" fill="none" stroke="#000000" stroke-width="1"/>
                
                <!-- Spires -->
                <polygon points="50,20 47,35 53,35" fill="none" stroke="#000000" stroke-width="1"/>
                <polygon points="35,35 33,45 37,45" fill="none" stroke="#000000" stroke-width="1"/>
                <polygon points="65,35 63,45 67,45" fill="none" stroke="#000000" stroke-width="1"/>
                
                <!-- Building base -->
                <rect x="30" y="60" width="40" height="20" fill="none" stroke="#000000" stroke-width="2"/>
                
                <text x="50" y="95" text-anchor="middle" font-size="6" fill="#000000">Red Square</text>
            </svg>
            """;
    }
    
    private String generateMumbaiGateway() {
        return """
            <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" style="width: 100px; height: 100px;">
                <!-- Main Gateway Structure -->
                <rect x="20" y="45" width="60" height="35" fill="none" stroke="#000000" stroke-width="2"/>
                
                <!-- Central Arch -->
                <path d="M 35,80 Q 50,60 65,80" fill="none" stroke="#000000" stroke-width="2"/>
                
                <!-- Side Towers -->
                <rect x="15" y="40" width="10" height="40" fill="none" stroke="#000000" stroke-width="2"/>
                <rect x="75" y="40" width="10" height="40" fill="none" stroke="#000000" stroke-width="2"/>
                
                <!-- Domes -->
                <circle cx="20" cy="35" r="8" fill="none" stroke="#000000" stroke-width="1.5"/>
                <circle cx="80" cy="35" r="8" fill="none" stroke="#000000" stroke-width="1.5"/>
                <circle cx="50" cy="35" r="10" fill="none" stroke="#000000" stroke-width="2"/>
                
                <!-- Decorative elements -->
                <line x1="35" y1="65" x2="65" y2="65" stroke="#000000" stroke-width="1"/>
                <line x1="40" y1="70" x2="60" y2="70" stroke="#000000" stroke-width="1"/>
                
                <text x="50" y="95" text-anchor="middle" font-size="6" fill="#000000">Gateway of India</text>
            </svg>
            """;
    }
    
    public String getCityEmoji(String cityName) {
        return switch (cityName.toLowerCase()) {
            case "london" -> "🏰"; // Big Ben/Tower
            case "paris" -> "🗼"; // Eiffel Tower
            case "tokyo" -> "🗼"; // Tokyo Tower
            case "new york", "newyork" -> "🗽"; // Statue of Liberty
            case "los angeles" -> "🎬"; // Hollywood
            case "san francisco", "sanfrancisco", "sf" -> "🌉"; // Golden Gate Bridge
            case "sydney" -> "🏛️"; // Opera House
            case "beijing" -> "🏯"; // Forbidden City
            case "shanghai" -> "🏙️"; // Skyline
            case "hong kong" -> "🏙️"; // Skyline
            case "dubai" -> "🏗️"; // Burj Khalifa
            case "mumbai" -> "🏛️"; // Gateway of India
            case "moscow" -> "🏛️"; // Red Square
            case "rome" -> "🏛️"; // Colosseum
            case "athens" -> "🏛️"; // Parthenon
            case "cairo" -> "🏺"; // Pyramid area
            case "rio de janeiro", "rio" -> "⛪"; // Christ Redeemer
            case "barcelona" -> "⛪"; // Sagrada Familia
            case "amsterdam" -> "🌷"; // Windmill/tulips
            case "berlin" -> "🏛️"; // Brandenburg Gate
            case "istanbul" -> "🕌"; // Hagia Sophia
            case "gothenburg", "göteborg" -> "🔱"; // Poseidon statue (trident)
            case "stockholm" -> "🏛️"; // City Hall
            case "copenhagen" -> "🧜‍♀️"; // Little Mermaid
            case "oslo" -> "🏛️"; // Opera House
            case "helsinki" -> "⛪"; // Cathedral
            case "reykjavik" -> "⛪"; // Hallgrímskirkja
            default -> "🏙️"; // Generic city
        };
    }
}