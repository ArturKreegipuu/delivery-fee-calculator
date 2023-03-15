package com.example.fooddelivery.deliveryfeecalculator.controller;

import com.example.fooddelivery.deliveryfeecalculator.model.WeatherData;
import com.example.fooddelivery.deliveryfeecalculator.repository.WeatherDataRepository;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
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
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class WeatherDataController {
    private WeatherDataRepository weatherDataRepository;
    private RestTemplate restTemplate;

    /**
     * Get request to get all the recorded weather data from the database
     * @return all the weather data from the database
     */
    @GetMapping("/weatherData")
    public Iterable<WeatherData> findAll() {
        return weatherDataRepository.findAll();
    }

    /**
     * Get request to find the latest weather information from specified station.
     * @param name name of a station
     * @return latest weather data from a spesific station
     */
    @GetMapping("/weatherData/{name}")
    public WeatherData findLatest(@PathVariable String name) {
        List<WeatherData> weatherDataList = (List<WeatherData>) weatherDataRepository.findAll();
        List<WeatherData> latestWeatherDataList = weatherDataList.stream()
                .filter(weatherData -> name.equals(weatherData.getName()))
                .collect(Collectors.toList());
        Collections.sort(latestWeatherDataList);
        return latestWeatherDataList.get(latestWeatherDataList.size()-1);
    }

    @GetMapping("/latestWeaherData")
    public List<WeatherData> fetchLatestWeatherData() {
        String url = "https://www.ilmateenistus.ee/ilma_andmed/xml/observations.php";
        String response = restTemplate.getForObject(url, String.class);

        List<WeatherData> weatherDataList = new ArrayList<>();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(response));
            Document doc = dBuilder.parse(inputSource);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("station");
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    WeatherData weatherData = new WeatherData();
                    String name = eElement.getElementsByTagName("name").item(0).getTextContent();
                    if (name.equals("Tartu-Tõravere") || name.equals("Pärnu") || name.equals("Tallinn-Harku")){
                        System.out.println(name);
                        weatherData.setName(name);
                        weatherData.setWMO(eElement.getElementsByTagName("wmocode").item(0).getTextContent());
                        weatherData.setAir_temp(Double.parseDouble(eElement.getElementsByTagName("airtemperature").item(0).getTextContent()));
                        weatherData.setWind_speed(Double.parseDouble(eElement.getElementsByTagName("windspeed").item(0).getTextContent()));
                        weatherData.setWeather_phenomenon(eElement.getElementsByTagName("phenomenon").item(0).getTextContent());
                        weatherData.setTimestamp(LocalDateTime.now());
                        weatherDataList.add(weatherData);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return weatherDataList;
    }
    @Scheduled(cron = "0 15 * * * *") // New weather data is inserted once every hour, 15 minutes after a full hour (HH:15:00).
    @PostMapping
    public void saveWeatherData() {
        List<WeatherData> weatherDataList = this.fetchLatestWeatherData();
        for (WeatherData weatherData : weatherDataList) {
            weatherDataRepository.save(weatherData);
        }
    }
}
