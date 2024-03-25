function fetchWeather() {
    const cityInput = document.getElementById('city');
    const city = cityInput.value;

    $.get("/weather?city=" + city, function(data) {
        // Update the content of the weatherDiv
        $('#weatherDiv').html(data);
    })
        .fail(function(xhr, status, error) {
            console.error('Error:', error);
            $('#weatherDiv').html("<p>Failed to fetch weather information.</p>");
        });
}
