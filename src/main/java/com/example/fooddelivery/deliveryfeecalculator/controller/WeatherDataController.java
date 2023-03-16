package com.example.fooddelivery.deliveryfeecalculator.controller;

import com.example.fooddelivery.deliveryfeecalculator.model.WeatherData;
import com.example.fooddelivery.deliveryfeecalculator.repository.WeatherDataRepository;
import com.example.fooddelivery.deliveryfeecalculator.service.DeliveryFeeService;
import com.example.fooddelivery.deliveryfeecalculator.service.WeatherDataService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * @return all the weather data from the database
     */
    @Operation(summary = "To fetch all the recorded weather data from the database")
    @GetMapping("/weatherData")
    public Iterable<WeatherData> findAll() {
        return weatherDataService.findAll();
    }


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
    public double calculateFee(@PathVariable("city") String city, @PathVariable("vehicle") String vehicle){
        return weatherDataService.calculateFee(city, vehicle);
    }

    /**
     * Get request to find the latest weather information for specified station from database.
     * @param name name of a station
     * @return latest weather data from a spesific station
     */
    @GetMapping("/latest/{name}")
    public WeatherData findLatest(@PathVariable String name) {
        return weatherDataService.findLatest(name);
    }

}
