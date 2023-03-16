package com.example.fooddelivery.deliveryfeecalculator.service;

import com.example.fooddelivery.deliveryfeecalculator.model.WeatherData;
import com.example.fooddelivery.deliveryfeecalculator.repository.WeatherDataRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WeatherDataService {
    private final WeatherDataRepository weatherDataRepository;
    private RestTemplate restTemplate;
    private DeliveryFeeService deliveryFeeService;

    @Autowired
    public WeatherDataService(WeatherDataRepository weatherDataRepository, RestTemplate restTemplate, DeliveryFeeService deliveryFeeService) {
        this.weatherDataRepository = weatherDataRepository;
        this.restTemplate = restTemplate;
        this.deliveryFeeService = deliveryFeeService;
    }

    public Iterable<WeatherData> findAll() {
        return weatherDataRepository.findAll();
    }

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

    public void saveWeatherData() {
        List<WeatherData> weatherDataList = this.fetchLatestWeatherData();
        for (WeatherData weatherData : weatherDataList) {
            weatherDataRepository.save(weatherData);
        }
    }


    public double calculateFee(String city, String vehicle){
        WeatherData weatherData = findLatest(city.toLowerCase());
        deliveryFeeService.setATEF(0);
        deliveryFeeService.setRBF(0);
        deliveryFeeService.setWPEF(0);
        deliveryFeeService.setWSEF(0);
        deliveryFeeService.setCity(city.toLowerCase());
        deliveryFeeService.setVehicle(vehicle.toLowerCase());
        deliveryFeeService.setWeatherData(weatherData);
        return deliveryFeeService.calculateFee();
    }

    public WeatherData findLatest(String name) {

        // Convert pathvariable to match name in the database
        name = getLocationName(name);

        List<WeatherData> weatherDataList = (List<WeatherData>) weatherDataRepository.findAll();
        String finalName = name;
        List<WeatherData> latestWeatherDataList = weatherDataList.stream()
                .filter(weatherData -> finalName.equals(weatherData.getName()))
                .collect(Collectors.toList());
        Collections.sort(latestWeatherDataList);
        return latestWeatherDataList.get(latestWeatherDataList.size()-1);
    }
    private String getLocationName(String alias) {
        switch (alias.toLowerCase()) {
            case "tartu":
                return "Tartu-Tõravere";
            case "tallinn":
                return "Tallinn-Harku";
            case "pärnu":
                return "Pärnu";
            default:
                return alias;
        }
    }
}
