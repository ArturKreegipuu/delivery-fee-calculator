package com.example.fooddelivery.deliveryfeecalculator.controller;

import com.example.fooddelivery.deliveryfeecalculator.exception.ApiRequestException;
import com.example.fooddelivery.deliveryfeecalculator.model.WeatherData;
import com.example.fooddelivery.deliveryfeecalculator.service.WeatherDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;


@RestController
@PropertySource("classpath:application.properties")
public class WeatherDataController {

    private WeatherDataService weatherDataService;

    @Autowired
    public WeatherDataController(WeatherDataService weatherDataService) {
        this.weatherDataService = weatherDataService;
    }


//    @Operation(summary = "To fetch all the recorded weather data from the database")

    /**
     * This method saves fresh weather data into the database once every hour, 15 minutes after a full hour (HH:15:00).
     * <p>
     * The method calls out weatherDataService's method saveWeatherData(), which calls out fetchLatestWeatherData() method
     * to fetch needed weather data to save into the database.
     */
    // The frequency of the cronjob is configurable in application.properties file.
    //@Scheduled(cron = "${app.weather.corn}") // New weather data is inserted once every hour, 15 minutes after a full hour (HH:15:00) == (cron = "0 15 * * * *").
    @Scheduled(fixedRateString = "${app.weather.fixedRate}")
    @PostMapping
    public void saveWeatherData() {
        weatherDataService.saveWeatherData();
    }

    /**
     * This method allows us to request calculated delivery fee based on recent weather data, city and vehicle.
     *
     * @param city    City of delivery. Allowed cities: Tallinn, Tartu, Pärnu
     * @param vehicle Vehicle of delivery. Allowed vehicles: Car, scooter, bike
     * @return The total delivery fee calculated using weather data, city and vehicle type
     * @throws ApiRequestException If any of the input parameters are incorrect or any business rules violated
     */
    @GetMapping("/fee/{city}/{vehicle}")
    public double calculateFee(@PathVariable("city") String city, @PathVariable("vehicle") String vehicle) throws ApiRequestException {
        double fee;
        try {
            fee = weatherDataService.calculateFee(city, vehicle);
        } catch (ApiRequestException e) {
            throw new ApiRequestException(e.getMessage());
        }
        return fee;
    }


    /**
     * Get request to find the latest weather information for specified station from database.
     *
     * @param stationName name of a station
     * @return latest weather data from a specific station
     */
    @GetMapping("/weather/{stationName}")
    public WeatherData findLatest(@PathVariable String stationName) throws ApiRequestException {
        return weatherDataService.findLatest(stationName);
    }

}
