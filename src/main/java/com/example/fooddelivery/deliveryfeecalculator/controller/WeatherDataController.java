package com.example.fooddelivery.deliveryfeecalculator.controller;

import com.example.fooddelivery.deliveryfeecalculator.exception.ApiRequestException;
import com.example.fooddelivery.deliveryfeecalculator.model.WeatherData;
import com.example.fooddelivery.deliveryfeecalculator.service.WeatherDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping
public class WeatherDataController {

    private WeatherDataService weatherDataService;

    @Autowired
    public WeatherDataController(WeatherDataService weatherDataService) {
        this.weatherDataService = weatherDataService;
    }

    /**
     * Get request to get all the recorded weather data from the database
     *
     * @return all the weather data from the database
     */
//    @Operation(summary = "To fetch all the recorded weather data from the database")
    @GetMapping("/latest")
    public List<WeatherData> fetchLatestWeatherData() {
        return weatherDataService.fetchLatestWeatherData();
    }

    // TODO: The frequency of the cronjob has to be configurable!!!1
    //@Scheduled(cron = "0 15 * * * *") // New weather data is inserted once every hour, 15 minutes after a full hour (HH:15:00).
    @Scheduled(fixedRate = 50000)
    @PostMapping
    public void saveWeatherData() {
        weatherDataService.saveWeatherData();
    }


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
     * @return latest weather data from a spesific station
     */
    @GetMapping
    public WeatherData findLatest(@PathVariable String stationName) throws ApiRequestException {
        return weatherDataService.findLatest(stationName);
    }

}
