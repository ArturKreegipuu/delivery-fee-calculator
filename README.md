# Delivery-fee-calculator

#### FUJITSU Java Programming Trial Task

#### Author: Artur Kreegipuu

Food delivery application that calculates the delivery fee for food couriers based on regional base fee, vehicle type, and weather conditions.

#### Scheduled task for importing weather data (CronJob) is configurable at application.properties file.

#### Once the application is running you can access:
* H2 database interface at: http://localhost:8080/h2-console
  * JDBC URL: jdbc:h2:mem:weatherData
  * User Name: sa

* Swagger UI documentation of the API at: http://localhost:8080/swagger-ui/index.html

#### GET request format to get the calculated delivery fee:
| Method | URI                       | Action                                                            |
|--------|---------------------------|-------------------------------------------------------------------|
| GET    | /weather/{city}/{vehicle} | Calculate delivery fee with input paremeters {city} and {vehicle} |
