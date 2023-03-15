package com.example.fooddelivery.deliveryfeecalculator.model;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class WeatherData implements Comparable<WeatherData>{
    @Id
    private Long id;
    private String name;
    private String WMO;
    private double air_temp;
    private double wind_speed;
    private String weather_phenomenon;
    private LocalDateTime timestamp;


    // Calculations must base on the latest weather data for a specific city so I will sort the list in order from the oldest to the latest
    /**
     * Method compares two WeatherData objects based on their timestamp.
     *
     * Collections.sort(weatherDataList) should return list of WeatherData objects that are ordered ascendingly based on their timestamp.
     * Latest weather data should be at the end of the list.
     * @param o Comparable WeatherData object
     * @return -1 if comparable WeatherData's(WeatherData o) timestamp is newer,
     * 1 if  comparable WeatherData's(WeatherData o) timestamp is older and 0 otherwise(both timestamps are somehow equal).
     */
    @Override
    public int compareTo(WeatherData o) {
        if (this.timestamp.isBefore(o.getTimestamp())) return -1;
        else if (this.timestamp.isAfter(o.getTimestamp())) return 1;
        return 0;
    }
}
