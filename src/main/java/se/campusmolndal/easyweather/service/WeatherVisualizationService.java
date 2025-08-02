package se.campusmolndal.easyweather.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.campusmolndal.easyweather.models.WeatherInfo;

@Service
public class WeatherVisualizationService {

    @Autowired
    private AIWeatherService aiWeatherService;
    
    @Autowired
    private LandmarkAnimationService landmarkAnimationService;

    public String generate3DWeatherScene(String city, WeatherInfo weatherInfo) {
        // Generate 3D scene based on weather conditions
        String sceneType = determineSceneType(weatherInfo);
        String timeOfDay = determineTimeOfDay();
        
        return build3DWeatherHTML(city, weatherInfo, sceneType, timeOfDay);
    }

    public String generateAIDescription(String city, WeatherInfo weatherInfo) {
        // Use AI service for enhanced descriptions
        return aiWeatherService.generateAIWeatherDescription(city, weatherInfo);
    }

    private String determineSceneType(WeatherInfo weatherInfo) {
        String description = weatherInfo.getDescription().toLowerCase();
        
        if (description.contains("snow")) return "winter";
        if (description.contains("rain")) return "rainy";
        if (description.contains("clear")) return "sunny";
        if (description.contains("cloud")) return "cloudy";
        if (description.contains("fog")) return "foggy";
        
        return "default";
    }

    private String determineTimeOfDay() {
        int hour = java.time.LocalTime.now().getHour();
        
        if (hour >= 6 && hour < 12) return "morning";
        if (hour >= 12 && hour < 18) return "afternoon";
        if (hour >= 18 && hour < 22) return "evening";
        
        return "night";
    }

    private String build3DWeatherHTML(String city, WeatherInfo weatherInfo, String sceneType, String timeOfDay) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>")
            .append("<html><head>")
            .append("<title>3D Weather - ").append(city).append("</title>")
            .append("<script src='https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js'></script>")
            .append(landmarkAnimationService.generateLandmarkCSS())
            .append("<style>")
            .append("body { margin: 0; padding: 0; background: linear-gradient(135deg, #74b9ff, #0984e3); }")
            .append("#weather-container { position: relative; width: 100vw; height: 100vh; }")
            .append("#weather-info { position: absolute; top: 20px; left: 20px; color: white; font-family: Arial; z-index: 100; }")
            .append("#weather-info h1 { font-size: 2.5em; margin: 0; text-shadow: 2px 2px 4px rgba(0,0,0,0.5); }")
            .append("#weather-info p { font-size: 1.2em; margin: 5px 0; text-shadow: 1px 1px 2px rgba(0,0,0,0.5); }")
            .append("#ai-description { position: absolute; top: 20px; right: 20px; width: 300px; color: white; font-family: Arial; font-style: italic; text-shadow: 1px 1px 2px rgba(0,0,0,0.5); background: rgba(0,0,0,0.3); padding: 15px; border-radius: 10px; }")
            .append("#landmark-section { position: absolute; bottom: 20px; left: 50%; transform: translateX(-50%); }")
            .append("</style>")
            .append("</head><body>")
            .append("<div id='weather-container'>")
            .append("<div id='weather-info'>")
            .append("<h1>").append(city).append("</h1>")
            .append("<p>Temperature: ").append(weatherInfo.getTemperature()).append("°C</p>")
            .append("<p>Wind Speed: ").append(weatherInfo.getWindSpeed()).append(" m/s</p>")
            .append("<p>Conditions: ").append(weatherInfo.getDescription()).append("</p>")
            .append("</div>")
            .append("<div id='ai-description'>")
            .append("<h3>🤖 AI Weather Story</h3>")
            .append("<p>").append(generateAIDescription(city, weatherInfo)).append("</p>")
            .append("</div>")
            .append("<div id='landmark-section'>")
            .append(landmarkAnimationService.generateLandmarkAnimation(city, weatherInfo))
            .append("</div>")
            .append("</div>")
            .append(generate3DScript(sceneType, timeOfDay, weatherInfo))
            .append("</body></html>");
        
        return html.toString();
    }

    private String generate3DScript(String sceneType, String timeOfDay, WeatherInfo weatherInfo) {
        return "<script>" +
                "// 3D Weather Scene" +
                "const scene = new THREE.Scene();" +
                "const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);" +
                "const renderer = new THREE.WebGLRenderer({ alpha: true });" +
                "renderer.setSize(window.innerWidth, window.innerHeight);" +
                "document.getElementById('weather-container').appendChild(renderer.domElement);" +
                
                // Set scene background based on weather
                getSceneBackground(sceneType, timeOfDay) +
                
                // Add weather particles
                getWeatherParticles(sceneType, weatherInfo) +
                
                // Animation loop
                "function animate() {" +
                "  requestAnimationFrame(animate);" +
                "  updateParticles();" +
                "  renderer.render(scene, camera);" +
                "}" +
                "animate();" +
                "</script>";
    }

    private String getSceneBackground(String sceneType, String timeOfDay) {
        switch (sceneType) {
            case "winter":
                return "scene.background = new THREE.Color(0x87CEEB);"; // Light blue for snow
            case "rainy":
                return "scene.background = new THREE.Color(0x708090);"; // Slate gray for rain
            case "sunny":
                return "scene.background = new THREE.Color(0x87CEFA);"; // Light sky blue
            default:
                return "scene.background = new THREE.Color(0x778899);"; // Light slate gray
        }
    }

    private String getWeatherParticles(String sceneType, WeatherInfo weatherInfo) {
        switch (sceneType) {
            case "winter":
                return generateSnowParticles();
            case "rainy":
                return generateRainParticles();
            default:
                return "// No particles for this weather type";
        }
    }

    private String generateSnowParticles() {
        return "const snowGeometry = new THREE.BufferGeometry();" +
               "const snowCount = 1000;" +
               "const snowPositions = new Float32Array(snowCount * 3);" +
               "for(let i = 0; i < snowCount * 3; i++) {" +
               "  snowPositions[i] = (Math.random() - 0.5) * 100;" +
               "}" +
               "snowGeometry.setAttribute('position', new THREE.BufferAttribute(snowPositions, 3));" +
               "const snowMaterial = new THREE.PointsMaterial({ color: 0xFFFFFF, size: 2 });" +
               "const snow = new THREE.Points(snowGeometry, snowMaterial);" +
               "scene.add(snow);" +
               "function updateParticles() {" +
               "  const positions = snow.geometry.attributes.position.array;" +
               "  for(let i = 1; i < positions.length; i += 3) {" +
               "    positions[i] -= 0.1;" +
               "    if(positions[i] < -50) positions[i] = 50;" +
               "  }" +
               "  snow.geometry.attributes.position.needsUpdate = true;" +
               "}";
    }

    private String generateRainParticles() {
        return "const rainGeometry = new THREE.BufferGeometry();" +
               "const rainCount = 2000;" +
               "const rainPositions = new Float32Array(rainCount * 3);" +
               "for(let i = 0; i < rainCount * 3; i++) {" +
               "  rainPositions[i] = (Math.random() - 0.5) * 100;" +
               "}" +
               "rainGeometry.setAttribute('position', new THREE.BufferAttribute(rainPositions, 3));" +
               "const rainMaterial = new THREE.PointsMaterial({ color: 0x4169E1, size: 1 });" +
               "const rain = new THREE.Points(rainGeometry, rainMaterial);" +
               "scene.add(rain);" +
               "function updateParticles() {" +
               "  const positions = rain.geometry.attributes.position.array;" +
               "  for(let i = 1; i < positions.length; i += 3) {" +
               "    positions[i] -= 0.5;" +
               "    if(positions[i] < -50) positions[i] = 50;" +
               "  }" +
               "  rain.geometry.attributes.position.needsUpdate = true;" +
               "}";
    }
}