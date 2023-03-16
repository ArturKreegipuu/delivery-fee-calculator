package com.example.fooddelivery.deliveryfeecalculator.controller;

import com.example.fooddelivery.deliveryfeecalculator.exception.ApiRequestException;
import com.example.fooddelivery.deliveryfeecalculator.model.WeatherData;
import com.example.fooddelivery.deliveryfeecalculator.service.DeliveryFeeCalculatorService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;


@RestController
@PropertySource("classpath:application.properties")
public class DeliveryFeeCalculatorController {

    private DeliveryFeeCalculatorService deliveryFeeCalculatorService;

    @Autowired
    public DeliveryFeeCalculatorController(DeliveryFeeCalculatorService deliveryFeeCalculatorService) {
        this.deliveryFeeCalculatorService = deliveryFeeCalculatorService;
    }

    /**
     * This method saves fresh weather data into the database once every hour, 15 minutes after a full hour (HH:15:00).
     * <p>
     * The method calls out deliveryFeeCalculatorService's method saveWeatherData(), which calls out fetchLatestWeatherData() method
     * to fetch needed weather data to save into the database.
     */

    @Operation(summary = "To save fresh weather data into the database once every hour")
    // The frequency of the cronjob is configurable in application.properties file.
    @Scheduled(cron = "${app.weather.cron}")
    // New weather data is inserted once every hour, 15 minutes after a full hour (HH:15:00) == (cron = "0 15 * * * *").
    //@Scheduled(fixedRateString = "${app.weather.fixedRate}")
    @PostMapping
    public void saveWeatherData() {
        deliveryFeeCalculatorService.saveWeatherData();
    }

    /**
     * This method allows us to request calculated delivery fee based on recent weather data, city and vehicle.
     *
     * @param city    City of delivery. Allowed cities: Tallinn, Tartu, Pärnu
     * @param vehicle Vehicle of delivery. Allowed vehicles: Car, scooter, bike
     * @return The total delivery fee calculated using weather data, city and vehicle type
     * @throws ApiRequestException If any of the input parameters are incorrect or any business rules violated
     */

    @Operation(summary = "To request calculated delivery fee based on recent weather data, city and vehicle.")
    @GetMapping("/fee/{city}/{vehicle}")
    public double calculateFee(@PathVariable("city") String city, @PathVariable("vehicle") String vehicle) throws ApiRequestException {
        double fee;
        try {
            fee = deliveryFeeCalculatorService.calculateFee(city, vehicle);
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
    @Operation(summary = "To get the latest weather information for specified station from database.")
    @GetMapping("/weather/{stationName}")
    public WeatherData findLatest(@PathVariable String stationName) throws ApiRequestException {
        return deliveryFeeCalculatorService.findLatest(stationName);
    }

}
