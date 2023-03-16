package com.example.fooddelivery.deliveryfeecalculator.service;

import com.example.fooddelivery.deliveryfeecalculator.exception.ApiRequestException;
import com.example.fooddelivery.deliveryfeecalculator.model.WeatherData;
import com.example.fooddelivery.deliveryfeecalculator.repository.WeatherDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
public class DeliveryFeeCalculatorService {
    private final WeatherDataRepository weatherDataRepository;
    private RestTemplate restTemplate;
    private DeliveryFeeCalculator deliveryFeeCalculator;

    @Autowired
    public DeliveryFeeCalculatorService(WeatherDataRepository weatherDataRepository, RestTemplate restTemplate, DeliveryFeeCalculator DeliveryFeeCalculator) {
        this.weatherDataRepository = weatherDataRepository;
        this.restTemplate = restTemplate;
        this.deliveryFeeCalculator = DeliveryFeeCalculator;
    }

    /**
     * This method fetches the latest weather data from the weather portal of the Estonian Environment Agency.
     * Only uses weather data of the following stations: Tallinn-Harku, Tartu-Tõravere, Pärnu.
     * Creates WeatherData objects with fetched data and saves them into a list.
     *
     * @return List containing weather data(WeatherData objects) af Tallinn-Harku, Tartu-Tõravere and Pärnu stations.
     */
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
                    if (name.equals("Tartu-Tõravere") || name.equals("Pärnu") || name.equals("Tallinn-Harku")) {
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

    /**
     * Saves freshly fetched weather data into the database.
     */
    public void saveWeatherData() {
        List<WeatherData> weatherDataList = this.fetchLatestWeatherData();
        for (WeatherData weatherData : weatherDataList) {
            weatherDataRepository.save(weatherData);
        }
    }

    /**
     * Calculates the delivery fee for a specific vehicle type in a given city based on the latest weather data available.
     * <p>
     * The city name is first converted to lowercase and passed to
     * the findLatest() method to retrieve the latest weather data for the city. The delivery fee calculator object is then
     * initialized with the appropriate values and the weather data, and the calculateFee() method is called to calculate
     * the delivery fee. The calculated fee is then returned.
     *
     * @param city    The name of the city for which to calculate the delivery fee.
     * @param vehicle The type of vehicle for which to calculate the delivery fee.
     * @return The calculated delivery fee for the specified vehicle type in the specified city.
     * @throws ApiRequestException If any of the input parameters are incorrect or any business rules violated.
     */
    public double calculateFee(String city, String vehicle) throws ApiRequestException {
        WeatherData weatherData = findLatest(city.toLowerCase());
        deliveryFeeCalculator.setATEF(0);
        deliveryFeeCalculator.setRBF(0);
        deliveryFeeCalculator.setWPEF(0);
        deliveryFeeCalculator.setWSEF(0);
        deliveryFeeCalculator.setCity(city.toLowerCase());
        deliveryFeeCalculator.setVehicle(vehicle.toLowerCase());
        deliveryFeeCalculator.setWeatherData(weatherData);
        return deliveryFeeCalculator.calculateFee();
    }

    /**
     * Finds the latest weather data for a specific city.
     * <p>
     * This method takes in the name of a city and returns the latest weather data available in the database for that location.
     * The city name is first checked if it matches one of the city names "tartu", "tallinn" or "pärnu".
     * If it matches, it is converted to the corresponding station name used in the weather data repository.
     *
     * @param name The name of the city for which to find the latest weather data.
     * @return The latest weather data for the specified location.
     * @throws ApiRequestException If an error occurs while retrieving the weather data from the repository.
     */
    public WeatherData findLatest(String name) throws ApiRequestException {

        // Check if city name is "tartu", "tallinn" or "pärnu" and convert it to match name in the database(station's name)
        name = getStationName(name);

        List<WeatherData> weatherDataList = (List<WeatherData>) weatherDataRepository.findAll();
        String finalName = name;
        List<WeatherData> latestWeatherDataList = weatherDataList.stream()
                .filter(weatherData -> finalName.equals(weatherData.getName()))
                .collect(Collectors.toList());
        Collections.sort(latestWeatherDataList);
        return latestWeatherDataList.get(latestWeatherDataList.size() - 1);
    }

    /**
     * Checks if city name is "tartu", "tallinn" or "pärnu" and convert it to match name in the database(station's name)
     *
     * @param alias station name to check
     * @return Correct full name of a weather station
     * @throws ApiRequestException If input name is not matching to Tartu, Tallinn or Pärnu
     */
    private String getStationName(String alias) throws ApiRequestException {
        switch (alias.toLowerCase()) {
            case "tartu":
                return "Tartu-Tõravere";
            case "tallinn":
                return "Tallinn-Harku";
            case "pärnu":
                return "Pärnu";
            default:
                throw new ApiRequestException("Invalid city!");
        }
    }
}
