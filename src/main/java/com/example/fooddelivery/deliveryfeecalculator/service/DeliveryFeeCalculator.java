package com.example.fooddelivery.deliveryfeecalculator.service;

import com.example.fooddelivery.deliveryfeecalculator.exception.ApiRequestException;
import com.example.fooddelivery.deliveryfeecalculator.model.WeatherData;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;


@Data
@Component
@NoArgsConstructor
public class DeliveryFeeCalculator {

    private String city;
    private String vehicle;
    private double RBF; // Regional base fee
    private double ATEF; // Air temperature extra fee
    private double WSEF; // Wind speed extra fee
    private double WPEF; // Weather phenomenon extra fee
    private WeatherData weatherData; // Weather data about this city


    public double calculateFee() throws ApiRequestException {
        if (!checkVehicle(vehicle)) throw new ApiRequestException("Invalid vehicle type!");
        else calculateRBF(this.city, this.vehicle);

        if (vehicle.equals("scooter") || vehicle.equals("bike")) {
            this.ATEF = calculateATEF();
            this.WPEF = calculateWPEF();
            if (vehicle.equals("bike")) this.WSEF = calculateWSEF();
        }

        return RBF + ATEF + WSEF + WPEF;
    }

    public boolean checkVehicle(String vehicle) {
        if (!vehicle.equals("car") && !vehicle.equals("bike") && !vehicle.equals("scooter"))
            return false;
        return true;
    }


    public void calculateRBF(String city, String vehicle) {
        switch (city) {
            case "tallinn":
                switch (vehicle) {
                    case "car":
                        RBF = 4;
                        break;
                    case "scooter":
                        RBF = 3.5;
                        break;
                    case "bike":
                        RBF = 3;
                        break;
                }
                break;
            case "tartu":
                switch (vehicle) {
                    case "car":
                        RBF = 3.5;
                        break;
                    case "scooter":
                        RBF = 3;
                        break;
                    case "bike":
                        RBF = 2.5;
                        break;
                }
                break;
            case "pärnu":
                switch (vehicle) {
                    case "car":
                        RBF = 3;
                        break;
                    case "scooter":
                        RBF = 2.5;
                        break;
                    case "bike":
                        RBF = 2;
                        break;
                }
                break;
        }
    }

    public double calculateATEF() {
        double temp = weatherData.getAir_temp();
        if (temp < -10) return 1;
        else if (temp <= 0) return 0.5;
        return 0;
    }

    public double calculateWSEF() throws ApiRequestException {
        double wind_speed = weatherData.getWind_speed();
        if (wind_speed > 20) throw new ApiRequestException("Usage of selected vehicle type is forbidden");
        else if (wind_speed <= 20 && wind_speed >= 10) return 0.5;
        return 0;
    }

    public double calculateWPEF() throws ApiRequestException {
        String phenomenon = weatherData.getWeather_phenomenon().toLowerCase();
        if (phenomenon.contains("snow") || phenomenon.contains("sleet")) return 1;
        else if (phenomenon.contains("rain")) return 0.5;
        else if (phenomenon.contains("glaze") || phenomenon.contains("hail") || phenomenon.contains("thunder"))
            throw new ApiRequestException("Usage of selected vehicle type is forbidden");
        return 0;
    }
}
