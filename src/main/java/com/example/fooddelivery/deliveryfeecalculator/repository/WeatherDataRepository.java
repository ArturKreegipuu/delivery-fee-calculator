package com.example.fooddelivery.deliveryfeecalculator.repository;

import com.example.fooddelivery.deliveryfeecalculator.model.WeatherData;
import org.springframework.data.repository.CrudRepository;

public interface WeatherDataRepository extends CrudRepository<WeatherData, Long> {
    WeatherData findLatestWeatherDataByName(String name);
}
