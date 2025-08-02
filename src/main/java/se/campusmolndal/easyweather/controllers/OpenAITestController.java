package se.campusmolndal.easyweather.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.campusmolndal.easyweather.service.AIWeatherService;
import se.campusmolndal.easyweather.service.CityLandmarkService;
import se.campusmolndal.easyweather.models.WeatherInfo;

@RestController
public class OpenAITestController {

    @Autowired
    private AIWeatherService aiWeatherService;
    
    @Autowired
    private CityLandmarkService cityLandmarkService;

    @GetMapping("/test/openai")
    public ResponseEntity<String> testOpenAI(@RequestParam(defaultValue = "Paris") String city) {
        try {
            // Create a test weather info
            WeatherInfo testWeather = new WeatherInfo(15.0, 5.2, "mostly clear", 0);

            StringBuilder result = new StringBuilder();
            result.append("<html><body>");
            result.append("<h2>OpenAI API Test Results</h2>");
            
            // Test 1: Weather Description Generation
            result.append("<h3>Test 1: Weather Description</h3>");
            try {
                String description = aiWeatherService.generateAIWeatherDescription(city, testWeather);
                result.append("<p><strong>Status:</strong> ✅ SUCCESS</p>");
                result.append("<p><strong>Generated Description:</strong></p>");
                result.append("<div style='background: #f0f0f0; padding: 10px; border-radius: 5px; margin: 10px 0;'>");
                result.append(description);
                result.append("</div>");
            } catch (Exception e) {
                result.append("<p><strong>Status:</strong> ❌ FAILED</p>");
                result.append("<p><strong>Error:</strong> ").append(e.getMessage()).append("</p>");
            }

            // Test 2: Landmark SVG Generation
            result.append("<h3>Test 2: Landmark SVG Generation</h3>");
            try {
                String landmarkSVG = aiWeatherService.generateAILandmarkSVG(city, testWeather);
                if (landmarkSVG != null && landmarkSVG.contains("<svg")) {
                    result.append("<p><strong>Status:</strong> ✅ SUCCESS</p>");
                    result.append("<p><strong>Generated Landmark:</strong></p>");
                    result.append("<div style='text-align: center; margin: 20px 0;'>");
                    result.append(landmarkSVG);
                    result.append("</div>");
                } else {
                    result.append("<p><strong>Status:</strong> ⚠️ FALLBACK USED</p>");
                    result.append("<p>AI generation failed, fallback SVG used:</p>");
                    result.append("<div style='text-align: center; margin: 20px 0;'>");
                    result.append(landmarkSVG);
                    result.append("</div>");
                }
            } catch (Exception e) {
                result.append("<p><strong>Status:</strong> ❌ FAILED</p>");
                result.append("<p><strong>Error:</strong> ").append(e.getMessage()).append("</p>");
            }

            // Test 3: API Key Status
            result.append("<h3>Test 3: Configuration Check</h3>");
            result.append("<p><strong>City:</strong> ").append(city).append("</p>");
            result.append("<p><strong>Test Weather:</strong> ").append(testWeather.getDescription())
                  .append(", ").append(testWeather.getTemperature()).append("°C</p>");

            result.append("</body></html>");
            return ResponseEntity.ok(result.toString());

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("<html><body><h2>Test Failed</h2><p>Error: " + e.getMessage() + "</p></body></html>");
        }
    }

    @GetMapping("/test/openai/simple")
    public ResponseEntity<String> simpleTest() {
        try {
            WeatherInfo testWeather = new WeatherInfo(20.0, 3.0, "sunny", 0);

            String result = aiWeatherService.generateAIWeatherDescription("London", testWeather);
            return ResponseEntity.ok("OpenAI Response: " + result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("OpenAI Test Failed: " + e.getMessage());
        }
    }
    
    @GetMapping("/test/combined")
    public ResponseEntity<String> testCombinedDisplay(@RequestParam(defaultValue = "Paris") String city) {
        try {
            // Create test weather info
            WeatherInfo testWeather = new WeatherInfo(18.0, 4.5, "partly cloudy", 2);
            
            // Generate combined display
            String combinedHTML = cityLandmarkService.generateCombinedWeatherDisplay(city, testWeather);
            
            // Wrap in full HTML
            StringBuilder result = new StringBuilder();
            result.append("<!DOCTYPE html><html><head>");
            result.append("<title>Weather Display Test - ").append(city).append("</title>");
            result.append("<meta charset='UTF-8'>");
            result.append("<style>");
            result.append("body { font-family: Arial, sans-serif; background: #f5f5f5; padding: 20px; }");
            result.append(".container { max-width: 800px; margin: 0 auto; }");
            result.append("h1 { text-align: center; color: #333; }");
            result.append("</style>");
            result.append("</head><body>");
            result.append("<div class='container'>");
            result.append("<h1>Weather & Landmark Display for ").append(city).append("</h1>");
            result.append(combinedHTML);
            result.append("<p style='text-align: center; margin-top: 30px;'>");
            result.append("<a href='/test/combined?city=London'>London</a> | ");
            result.append("<a href='/test/combined?city=Tokyo'>Tokyo</a> | ");
            result.append("<a href='/test/combined?city=New York'>New York</a> | ");
            result.append("<a href='/test/combined?city=Sydney'>Sydney</a>");
            result.append("</p>");
            result.append("</div></body></html>");
            
            return ResponseEntity.ok(result.toString());
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("<html><body><h2>Combined Test Failed</h2><p>Error: " + e.getMessage() + "</p></body></html>");
        }
    }
}