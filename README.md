# Weather Searching App on ApsaraDB RDS and Alibaba Cloud ECS

This application is a development from a Swedish weather searching app, designed to provide weather information for various cities, with a focus on Swedish cities. The application is built using Spring Boot and runs on an Alibaba Cloud Elastic Compute Service (ECS) instance, with the data stored in an ApsaraDB RDS (Relational Database Service) MySQL database.

## City Manager

The CityManager class is responsible for managing the cities. It stores Swedish cities along with their respective latitude and longitude coordinates. This data is used to fetch weather information from the weather API.

## OpenCageData API

To enhance the functionality of the application, the OpenCageData API is used. This API allows the conversion of city names into geographical coordinates (latitude and longitude). When a new city is inputted, the OpenCageData API is used to obtain the city's coordinates. These coordinates are then stored in the ApsaraDB RDS MySQL database, specifically in the weather table. The table stores the city name, latitude, and longitude.

### API Key Setup

1. Sign up for a free account at [OpenCage Geocoding API](https://opencagedata.com/)
2. Get your API key from the dashboard
3. Update `src/main/resources/application.properties`:
   ```properties
   opencage.api.key=YOUR_ACTUAL_API_KEY_HERE
   ```
4. For Heroku deployment, set the environment variable:
   ```bash
   heroku config:set OPENCAGE_API_KEY=your_actual_api_key_here
   ```

## Open-Meteo.com

The application uses the Open-Meteo.com API to fetch weather information using the latitude and longitude of a city. When a city name is inputted, the application first checks if the city's coordinates are already stored in the ApsaraDB RDS database. If they are, the application fetches the weather information directly from Open-Meteo.com using these coordinates. If the city's coordinates are not in the database, the application uses the OpenCageData API to obtain the coordinates, stores them in the ApsaraDB RDS database, and then fetches the weather information from Open-Meteo.com.

## Usage

To use the application, simply input the name of the city you want to check the weather for. The application will handle the rest, providing you with the current weather information for that city.

## Troubleshooting

### Common Issues

**404 Error**: Make sure your frontend is calling the correct endpoint (`/api/weather` or `/weather`)

**500 Internal Server Error**: 
- Check that your OpenCage API key is properly configured
- Verify the API key is valid and has remaining quota
- Check application logs for detailed error messages

**City Not Found**: 
- Verify the city name spelling
- Try using the full city name or include country (e.g., "Stockholm, Sweden")

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/bluehawana/SpringBoot-WeatherApp-HerokuCloud.git
   cd SpringBoot-WeatherApp-HerokuCloud
   ```

2. **Set up configuration**
   ```bash
   # Copy the template file
   cp src/main/resources/application.properties.template src/main/resources/application.properties
   
   # Edit application.properties and add your API keys:
   # - Get OpenCage API key from https://opencagedata.com/
   # - Get OpenAI API key from https://platform.openai.com/
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Access the application**
   - Main app: `http://localhost:8080`
   - Immersive experience: `http://localhost:8080/immersive.html`
   - Demo page: `http://localhost:8080/demo.html`

### Environment Variables for Production

Set these environment variables in your production environment (Heroku):

```bash
# Required
OPENCAGE_API_KEY=your_opencage_api_key_here
JAWSDB_URL=mysql://username:password@host:port/database

# Optional
OPENAI_API_KEY=your_openai_api_key_here
DATABASE_DRIVER=com.mysql.cj.jdbc.Driver
```

## Deployment

This application is deployed on an Alibaba Cloud Elastic Compute Service (ECS) instance, with the data stored in an ApsaraDB RDS MySQL database. This deployment architecture ensures high availability, scalability, and reliable data storage for the application.

## Development

This application was developed with Java and Spring Boot, using Maven for dependency management. It interacts with the OpenCageData and Open-Meteo.com APIs for data retrieval. The core logic is to achieve the goal of checking weather in any city in the world by inputting its name. Later, there will be Android and iOS versions as well, and they will keep the same black-white console style.

## Future Improvements

Future improvements to this application could include expanding the list of cities, improving the user interface, adding more detailed weather information, and implementing caching mechanisms to improve performance.

## License

The license for this project can be found in the [LICENSE.md](LICENSE) file.

## Author
Hongzhi Li
