package com.example.fooddelivery.deliveryfeecalculator.service;

import com.example.fooddelivery.deliveryfeecalculator.exception.VehicleException;
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
    private double RBF; // regional base fee
    private double ATEF; // air temperature extra fee
    private double WSEF; // wind speed extra fee
    private double WPEF; // weather phenomenon extra fee
    private WeatherData weatherData;


    public double calculateFee() {
        try {
            if (vehicle.equals("scooter") || vehicle.equals("bike")) {
                this.ATEF = calculateATEF();
                this.WPEF = calculateWPEF();
                if (vehicle.equals("bike")) this.WSEF = calculateWSEF();
            }
        } catch (VehicleException e) {
            e.getMessage();
        }
        calculateRBF(this.city, this.vehicle);
        return RBF + ATEF + WSEF + WPEF;
    }


    private void calculateRBF(String city, String vehicle) {
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
            default:
                throw new RuntimeException("Invalid input parameters");
        }
    }

    private double calculateATEF() {
        double temp = weatherData.getAir_temp();
        if (temp < -10) return 1;
        else if (temp <= 0) return 0.5;
        return 0;
    }

    private double calculateWSEF() throws VehicleException {
        double wind_speed = weatherData.getWind_speed();
        if (wind_speed > 20) throw new VehicleException("Usage of selected vehicle type is forbidden");
        else if (wind_speed <= 20 && wind_speed >= 10) return 0.5;
        return 0;
    }

    private double calculateWPEF() throws VehicleException {
        String phenomenon = weatherData.getWeather_phenomenon().toLowerCase();
        if (phenomenon.contains("snow") || phenomenon.contains("sleet")) return 1;
        else if (phenomenon.contains("rain")) return 0.5;
        else if (phenomenon.contains("glaze") || phenomenon.contains("hail") || phenomenon.contains("thunder"))
            throw new VehicleException("Usage of selected vehicle type is forbidden");
        return 0;
    }
}
