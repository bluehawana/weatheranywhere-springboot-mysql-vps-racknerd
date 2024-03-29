function fetchWeather() {
    const cityInput = document.getElementById('city');
    const city = cityInput.value;

    $.get("/api/weather?city=" + city, function(data) {
        // Update the content of the weatherDiv
        $('#weatherDiv').html(data);

        // Redirect to a new URL with the city parameter
        window.location = "/weather?city=" + city;
    })
        .fail(function(xhr, status, error) {
            console.error('Error:', error);
            $('#weatherDiv').html("<p>Failed to fetch weather information.</p>");
        });
    }
