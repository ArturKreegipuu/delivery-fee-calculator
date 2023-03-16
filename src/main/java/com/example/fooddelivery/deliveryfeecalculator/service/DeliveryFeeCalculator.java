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

    private String city; // City for which to calculate the delivery fee.
    private String vehicle; // Vehicle for which to calculate the delivery fee.
    private double RBF; // Regional base fee
    private double ATEF; // Air temperature extra fee
    private double WSEF; // Wind speed extra fee
    private double WPEF; // Weather phenomenon extra fee
    private WeatherData weatherData; // Weather data about this city

    /**
     * Calculates the delivery fee based on the values set for the deliveryFeeCalculator object.
     * <p>
     * This method calculates the delivery fee based on the values set for the deliveryFeeCalculator object,
     * including the city, vehicle, and weather data.
     * It first checks if the vehicle type is valid, and if not, throws an ApiRequestException.
     * Otherwise, it calculates the regional base fee (RBF) for the given city and vehicle type, and if the vehicle type is a
     * bike or a scooter, calculates additional fees such as the air temperature extra fee (ATEF),
     * the wind speed extra fee (WPEF), and the weather phenomenon extra fee (WSEF).
     * Finally, it returns the sum of all the calculated fees.
     *
     * @return The calculated delivery fee based on the values set for the deliveryFeeCalculator object.
     * @throws ApiRequestException If vehicle type is incorrect or any business rules violated.
     */
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

    /**
     * Checks if vehicle type is valid(car, bike or scooter).
     *
     * @param vehicle Vehicle that needs to be checked.
     * @return true if vehicle is valid, false if vehicle is invalid.
     */
    public boolean checkVehicle(String vehicle) {
        if (!vehicle.equals("car") && !vehicle.equals("bike") && !vehicle.equals("scooter"))
            return false;
        return true;
    }

    /**
     * Calculates the regional base fee (RBF) for the given city and vehicle combination
     * according to the business rules introduced in the task description.
     *
     * @param city    City the name of the city where the delivery is being made
     * @param vehicle The type of vehicle that will be used for the delivery
     */
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

    /**
     * Calculates air temperature extra fee(ATEF) based on air temperature according to the business rules introduced in the task description.
     *
     * @return Air temperature extra fee(ATEF)
     */
    public double calculateATEF() {
        double temp = weatherData.getAir_temp();
        if (temp < -10) return 1;
        else if (temp <= 0) return 0.5;
        return 0;
    }

    /**
     * Calculates wind speed extra fee(WSEF) based on wind speed according to the business rules introduced in the task description.
     *
     * @return Wind speed extra fee(WSEF)
     * @throws ApiRequestException If wind speed is larger than 20
     */
    public double calculateWSEF() throws ApiRequestException {
        double wind_speed = weatherData.getWind_speed();
        if (wind_speed > 20) throw new ApiRequestException("Usage of selected vehicle type is forbidden");
        else if (wind_speed <= 20 && wind_speed >= 10) return 0.5;
        return 0;
    }

    /**
     * Calculates weather phenomenon extra fee(WPEF) based on weather phenomenon according to the business rules introduced in the task description.
     *
     * @return Weather phenomenon extra fee(WPEF)
     * @throws ApiRequestException If weather phenomenon is glaze, hail, or thunder.
     */
    public double calculateWPEF() throws ApiRequestException {
        String phenomenon = weatherData.getWeather_phenomenon().toLowerCase();
        if (phenomenon.contains("snow") || phenomenon.contains("sleet")) return 1;
        else if (phenomenon.contains("rain")) return 0.5;
        else if (phenomenon.contains("glaze") || phenomenon.contains("hail") || phenomenon.contains("thunder"))
            throw new ApiRequestException("Usage of selected vehicle type is forbidden");
        return 0;
    }
}
