-- Create the cities table in JawsDB MySQL
CREATE TABLE IF NOT EXISTS cities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cityName VARCHAR(255) NOT NULL UNIQUE,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL
);

-- Insert some common cities to get started
INSERT IGNORE INTO cities (cityName, latitude, longitude) VALUES
('Stockholm', 59.3293, 18.0686),
('Göteborg', 57.7089, 11.9746),
('Gothenburg', 57.7089, 11.9746),
('Malmö', 55.6044, 13.0038),
('London', 51.5074, -0.1278),
('New York', 40.7128, -74.0060),
('Tokyo', 35.6762, 139.6503),
('Shanghai', 31.2304, 121.4737),
('Mohe', 52.9667, 122.5167);