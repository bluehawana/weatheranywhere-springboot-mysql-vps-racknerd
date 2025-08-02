function fetchWeather() {
    const cityInput = document.getElementById('city');
    const city = cityInput.value;

    if (!city.trim()) {
        $('#weatherDiv').html("<p>Please enter a city name.</p>");
        return;
    }

    // Show loading message
    $('#weatherDiv').html("<p>Loading weather information...</p>");

    $.get("/api/weather?city=" + encodeURIComponent(city), function(data) {
        // Update the content of the weatherDiv
        $('#weatherDiv').html(data);
    })
        .fail(function(xhr, status, error) {
            console.error('Error:', error);
            let errorMessage = "Failed to fetch weather information.";
            if (xhr.status === 404) {
                errorMessage = "City not found. Please check the spelling and try again.";
            } else if (xhr.status === 500) {
                errorMessage = "Server error. Please try again later.";
            }
            $('#weatherDiv').html("<p>" + errorMessage + "</p>");
        });
}
