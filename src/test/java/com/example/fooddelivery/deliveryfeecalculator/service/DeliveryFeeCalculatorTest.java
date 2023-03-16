package com.example.fooddelivery.deliveryfeecalculator.service;

import com.example.fooddelivery.deliveryfeecalculator.exception.ApiRequestException;
import com.example.fooddelivery.deliveryfeecalculator.model.WeatherData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryFeeCalculatorTest {
    private DeliveryFeeCalculator deliveryFeeCalculator;
    private WeatherData weatherData;

    @BeforeEach
    void setUp() {
        deliveryFeeCalculator = new DeliveryFeeCalculator();
        weatherData = new WeatherData();
    }


    /**
     * Test if vehicle is not "car", "scooter" or "bike"
     */
    @Test
    void incorrectVehicle() {
        assertEquals(false, deliveryFeeCalculator.checkVehicle("skateboard"));
    }

    /**
     * Test if regional base fee is calculated correctly
     */
    @Test
    void testRBF() {
        // For Tallinn
        deliveryFeeCalculator.calculateRBF("tallinn", "car");
        assertEquals(4, deliveryFeeCalculator.getRBF());
        deliveryFeeCalculator.calculateRBF("tallinn", "scooter");
        assertEquals(3.5, deliveryFeeCalculator.getRBF());
        deliveryFeeCalculator.calculateRBF("tallinn", "bike");
        assertEquals(3, deliveryFeeCalculator.getRBF());
        // For Tartu
        deliveryFeeCalculator.calculateRBF("tartu", "car");
        assertEquals(3.5, deliveryFeeCalculator.getRBF());
        deliveryFeeCalculator.calculateRBF("tartu", "scooter");
        assertEquals(3, deliveryFeeCalculator.getRBF());
        deliveryFeeCalculator.calculateRBF("tartu", "bike");
        assertEquals(2.5, deliveryFeeCalculator.getRBF());
        // For Pärnu
        deliveryFeeCalculator.calculateRBF("pärnu", "car");
        assertEquals(3, deliveryFeeCalculator.getRBF());
        deliveryFeeCalculator.calculateRBF("pärnu", "scooter");
        assertEquals(2.5, deliveryFeeCalculator.getRBF());
        deliveryFeeCalculator.calculateRBF("pärnu", "bike");
        assertEquals(2, deliveryFeeCalculator.getRBF());
    }

    /**
     * Test if air temperature extra fee is calculated correctly.
     * We assume that vehicle type is "scooter" or "bike"
     */
    @Test
    void testATEF() {

        // Air temperature is less than -10̊ C, then ATEF = 1 €
        weatherData.setAir_temp(-20);
        deliveryFeeCalculator.setWeatherData(weatherData);
        assertEquals(1, deliveryFeeCalculator.calculateATEF());

        // Air temperature is between -10̊ C and 0̊ C, then ATEF = 0,5 €
        weatherData.setAir_temp(-5);
        deliveryFeeCalculator.setWeatherData(weatherData);
        assertEquals(0.5, deliveryFeeCalculator.calculateATEF());

        // Air temperature is warmer than 0, then ATEF = 0 €
        weatherData.setAir_temp(5);
        deliveryFeeCalculator.setWeatherData(weatherData);
        assertEquals(0, deliveryFeeCalculator.calculateATEF());
    }

    /**
     * Test if wind speed extra fee is calculated correctly.
     * We assume that vehicle type is "bike"
     */
    @Test
    void testWSEF() throws ApiRequestException {
        // Wind speed is between 10 m/s and 20 m/s, then WSEF = 0,5 €
        weatherData.setWind_speed(15);
        deliveryFeeCalculator.setWeatherData(weatherData);
        assertEquals(0.5, deliveryFeeCalculator.calculateWSEF());

        // In case of wind speed is greater than 20 m/s, then the error message “Usage of selected vehicle type is forbidden” has to be given
        weatherData.setWind_speed(25);
        deliveryFeeCalculator.setWeatherData(weatherData);
        ApiRequestException thrown = assertThrows(ApiRequestException.class, () ->
                deliveryFeeCalculator.calculateWSEF(), "ApiRequestException error was expected"
        );
        assertTrue(thrown.getMessage().contentEquals("Usage of selected vehicle type is forbidden"));
        // WSEF should be equal to 0
        assertEquals(0, deliveryFeeCalculator.getWSEF());
    }

    /**
     * Test if weather phenomenon extra fee is calculated correctly.
     * We assume that vehicle type is "scooter" or "bike"
     */
    @Test
    void testWPEF() throws ApiRequestException {
        // Weather phenomenon is related to snow or sleet, then WPEF = 1 €
        weatherData.setWeather_phenomenon("light snow");
        deliveryFeeCalculator.setWeatherData(weatherData);
        assertEquals(1, deliveryFeeCalculator.calculateWPEF());
        weatherData.setWeather_phenomenon("sleet");
        deliveryFeeCalculator.setWeatherData(weatherData);
        assertEquals(1, deliveryFeeCalculator.calculateWPEF());

        // Weather phenomenon is related to rain, then WPEF = 0,5 €
        weatherData.setWeather_phenomenon("heavy rain");
        deliveryFeeCalculator.setWeatherData(weatherData);
        assertEquals(0.5, deliveryFeeCalculator.calculateWPEF());

        // In case the weather phenomenon is glaze, hail, or thunder, then the error message “Usage of
        // selected vehicle type is forbidden” has to be given
        weatherData.setWeather_phenomenon("glaze");
        deliveryFeeCalculator.setWeatherData(weatherData);
        ApiRequestException thrown = assertThrows(ApiRequestException.class, () ->
                deliveryFeeCalculator.calculateWPEF(), "ApiRequestException error was expected"
        );
        assertTrue(thrown.getMessage().contentEquals("Usage of selected vehicle type is forbidden"));
        // WPEF should be equal to 0
        assertEquals(0, deliveryFeeCalculator.getWPEF());

        weatherData.setWeather_phenomenon("hail");
        deliveryFeeCalculator.setWeatherData(weatherData);
        ApiRequestException thrown2 = assertThrows(ApiRequestException.class, () ->
                deliveryFeeCalculator.calculateWPEF(), "ApiRequestException error was expected"
        );
        assertTrue(thrown2.getMessage().contentEquals("Usage of selected vehicle type is forbidden"));
        // WPEF should be equal to 0
        assertEquals(0, deliveryFeeCalculator.getWPEF());

        weatherData.setWeather_phenomenon("thunder");
        deliveryFeeCalculator.setWeatherData(weatherData);
        ApiRequestException thrown3 = assertThrows(ApiRequestException.class, () ->
                deliveryFeeCalculator.calculateWPEF(), "ApiRequestException error was expected"
        );
        assertTrue(thrown3.getMessage().contentEquals("Usage of selected vehicle type is forbidden"));
        // WPEF should be equal to 0
        assertEquals(0, deliveryFeeCalculator.getWPEF());
    }

    /**
     * Test if delivery fee is calculated correctly
     */
    @Test
    void testCalculateFee() throws ApiRequestException {
        weatherData.setAir_temp(8);
        weatherData.setWind_speed(9);
        weatherData.setWeather_phenomenon("Light rain");
        deliveryFeeCalculator.setWeatherData(weatherData);
        deliveryFeeCalculator.setCity("pärnu");
        deliveryFeeCalculator.setVehicle("scooter");

        assertEquals(3, deliveryFeeCalculator.calculateFee()); // 2.5 + 0 + 0 + 0.5

    }

    /**
     * Test that air temperature extra fee is not calculated if vehicle is "car"
     * and is calculated if vehicle is "scooter" or "bike" in calculateFee() method
     */
    @Test
    void testATEFType() throws ApiRequestException {
        weatherData.setAir_temp(-11);
        weatherData.setWind_speed(9);
        weatherData.setWeather_phenomenon("clear");
        deliveryFeeCalculator.setWeatherData(weatherData);
        deliveryFeeCalculator.setCity("tartu");

        // For car ATEF should be 0
        deliveryFeeCalculator.setVehicle("car");
        deliveryFeeCalculator.calculateFee();
        assertEquals(0, deliveryFeeCalculator.getATEF());

        // For bike ATEF should be 1
        deliveryFeeCalculator.setATEF(0);
        deliveryFeeCalculator.setVehicle("bike");
        deliveryFeeCalculator.calculateFee();
        assertEquals(1, deliveryFeeCalculator.getATEF());

        // For scooter ATEF should be 1
        deliveryFeeCalculator.setWPEF(0);
        deliveryFeeCalculator.setVehicle("scooter");
        deliveryFeeCalculator.calculateFee();
        assertEquals(1, deliveryFeeCalculator.getATEF());
    }

    /**
     * Test that weather phenomenon extra fee is not calculated if vehicle is "car"
     * and is calculated if vehicle is "scooter" or "bike" in calculateFee() method
     */
    @Test
    void testWPEFType() throws ApiRequestException {
        weatherData.setAir_temp(-11);
        weatherData.setWind_speed(9);
        weatherData.setWeather_phenomenon("light snow");
        deliveryFeeCalculator.setWeatherData(weatherData);
        deliveryFeeCalculator.setCity("tartu");

        // For car WPEF should be 0
        deliveryFeeCalculator.setVehicle("car");
        deliveryFeeCalculator.calculateFee();
        assertEquals(0, deliveryFeeCalculator.getWPEF());

        // For bike WPEF should be 1
        deliveryFeeCalculator.setWPEF(0);
        deliveryFeeCalculator.setVehicle("bike");
        deliveryFeeCalculator.calculateFee();
        assertEquals(1, deliveryFeeCalculator.getWPEF());

        // For scooter WPEF should be 1
        deliveryFeeCalculator.setWPEF(0);
        deliveryFeeCalculator.setVehicle("scooter");
        deliveryFeeCalculator.calculateFee();
        assertEquals(1, deliveryFeeCalculator.getWPEF());
    }

    /**
     * Test that wind speed extra fee is not calculated if vehicle is "car" or "scooter"
     * and is calculated if vehicle is "bike" in calculateFee() method
     */
    @Test
    void testWSEFType() throws ApiRequestException {
        weatherData.setAir_temp(-11);
        weatherData.setWind_speed(15);
        weatherData.setWeather_phenomenon("light snow");
        deliveryFeeCalculator.setWeatherData(weatherData);
        deliveryFeeCalculator.setCity("tartu");

        // For car WSEF should be 0
        deliveryFeeCalculator.setVehicle("car");
        deliveryFeeCalculator.calculateFee();
        assertEquals(0, deliveryFeeCalculator.getWSEF());

        // For bike WSEF should be 0.5
        deliveryFeeCalculator.setWSEF(0);
        deliveryFeeCalculator.setVehicle("bike");
        deliveryFeeCalculator.calculateFee();
        assertEquals(0.5, deliveryFeeCalculator.getWSEF());

        // For scooter WSEF should be 1
        deliveryFeeCalculator.setWSEF(0);
        deliveryFeeCalculator.setVehicle("scooter");
        deliveryFeeCalculator.calculateFee();
        assertEquals(0, deliveryFeeCalculator.getWSEF());
    }
}