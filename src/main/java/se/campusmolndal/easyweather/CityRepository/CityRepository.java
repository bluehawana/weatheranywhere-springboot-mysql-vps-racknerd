package se.campusmolndal.easyweather.CityRepository;
import se.campusmolndal.easyweather.models.City;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;



public class CityRepository {
    private Map<String, City> cityMap;

    public CityRepository() {
        this.cityMap = new HashMap<>();
    }

    public void addCity(City city) {
        cityMap.put(city.getName(), city);
    }

    public City getCity(String cityName) {
        return cityMap.get(cityName);
    }

    public Optional<City> findByName(String cityName) {
        return Optional.ofNullable(cityMap.get(cityName));
    }

    public void save(City city) {
        cityMap.put(city.getName(), city);
    }
}