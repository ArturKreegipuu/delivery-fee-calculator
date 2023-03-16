package com.example.fooddelivery.deliveryfeecalculator.repository;

import com.example.fooddelivery.deliveryfeecalculator.model.WeatherData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherDataRepository extends CrudRepository<WeatherData, Long> {
}
