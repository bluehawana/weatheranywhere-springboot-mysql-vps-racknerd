# WeatherAnywhere - Spring Boot Weather Application

A full-stack weather application built with Spring Boot, MySQL, and deployed on RackNerd VPS.

## Features

- 🌍 **Global Weather Data**: Get weather information for any city worldwide
- 🎨 **Auto Theme Switching**: White theme (8 AM - 6 PM), Black theme (6 PM - 8 AM) based on local time
- 🏛️ **AI-Generated Landmarks**: Beautiful SVG landmark icons for cities using OpenAI
- 🌦️ **ASCII Weather Art**: Retro ASCII art weather icons
- 📍 **Geocoding Integration**: Location-aware features using OpenCage API
- ⚡ **REST API**: Clean REST endpoints for weather data

## Tech Stack

- **Backend**: Spring Boot 3.2.4, Java 17
- **Database**: MySQL 8.0
- **Frontend**: HTML, CSS, JavaScript (jQuery)
- **APIs**: OpenWeatherMap, OpenAI, OpenCage Geocoding
- **Deployment**: RackNerd VPS, Nginx, systemd

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.6+
- MySQL 8.0+

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/bluehawana/weatheranywhere-springboot-mysql-vps-racknerd.git
   cd weatheranywhere-springboot-mysql-vps-racknerd
   ```

2. **Configure environment variables**
   ```bash
   cp .env.example .env
   # Edit .env with your actual credentials
   ```

3. **Create database**
   ```bash
   mysql -u root -p
   CREATE DATABASE weatheranywhere;
   ```

4. **Build and run**
   ```bash
   mvn clean package
   java -jar target/EasyWeather-0.0.1-SNAPSHOT.jar
   ```

5. **Access the application**
   ```
   http://localhost:8080
   ```

## Environment Variables

See `.env.example` for required configuration:

- `OPENCAGE_API_KEY`: Get from [OpenCage](https://opencagedata.com/)
- `OPENAI_API_KEY`: Get from [OpenAI](https://platform.openai.com/)
- Database credentials
- VPS configuration (for deployment)

## Deployment

### RackNerd VPS

1. Build the JAR:
   ```bash
   mvn clean package
   ```

2. Deploy to VPS:
   ```bash
   scp target/EasyWeather-0.0.1-SNAPSHOT.jar user@your-vps:/opt/weatheranywhere/
   ```

3. Restart service:
   ```bash
   ssh user@your-vps "sudo systemctl restart weatheranywhere"
   ```

See `scripts/` directory for deployment scripts and systemd service configuration.

## API Endpoints

- `GET /` - Web interface
- `GET /weather?city={cityName}` - Get weather for a city
- `GET /api/weather?city={cityName}` - JSON weather data

## Project Structure

```
weatheranywhere/
├── src/main/
│   ├── java/se/campusmolndal/easyweather/
│   │   ├── controllers/    # REST controllers
│   │   ├── models/        # Data models
│   │   └── service/       # Business logic
│   └── resources/
│       ├── static/        # HTML, CSS, JS
│       └── application.properties.template
├── scripts/               # Deployment scripts
├── .env.example          # Environment template
└── pom.xml
```

## Features in Detail

### Automatic Theme Switching
The UI automatically switches between light and dark themes based on time:
- **White theme**: 08:00 - 18:00 (daytime)
- **Black theme**: 18:00 - 08:00 (nighttime)

Uses the user's local time zone for automatic detection.

### AI-Generated Landmarks
- Generates custom SVG landmarks for cities using OpenAI
- Falls back to hand-crafted SVGs for major cities
- Emoji fallback for ultimate compatibility

### Clean Logging
- Minimal console output
- Silent API fallbacks
- No verbose error messages cluttering the UI

## License

MIT License - feel free to use this project for learning and development.

## Contributing

Contributions welcome! Please open an issue or submit a PR.

## Author

**bluehawana**
- GitHub: [@bluehawana](https://github.com/bluehawana)

---

Built with ☕ and Spring Boot
