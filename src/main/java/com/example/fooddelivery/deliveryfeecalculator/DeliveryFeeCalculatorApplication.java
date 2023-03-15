package com.example.fooddelivery.deliveryfeecalculator;

import com.example.fooddelivery.deliveryfeecalculator.controller.WeatherDataController;
import com.example.fooddelivery.deliveryfeecalculator.model.WeatherData;
import com.example.fooddelivery.deliveryfeecalculator.repository.WeatherDataRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@SpringBootApplication
@EnableScheduling
public class DeliveryFeeCalculatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeliveryFeeCalculatorApplication.class, args);
    }

//    @GetMapping()
//    public String foodDeliveryApplication() {
//        return "Welcome to the food delivery application.";
//    }

    // Seeding data to test
//    @Bean
//    CommandLineRunner commandLineRunner(WeatherDataRepository weatherDataRepository) {
//		return args -> {
//          weatherDataRepository.save(new WeatherData(null, "Pärnu", "41803", 2.9, 4.4, "clear", LocalDateTime.now()));
//          weatherDataRepository.save(new WeatherData(null, "Tartu", "41803", 2.9, 4.4, "clear", LocalDateTime.now()));
//          weatherDataRepository.save(new WeatherData(null, "Tartu", "41804", 2.9, 4.4, "clear", LocalDateTime.now()));
//
//        };
//    }
    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }


}
