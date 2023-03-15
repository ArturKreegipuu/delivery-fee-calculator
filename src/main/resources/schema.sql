create table WEATHER_DATA(
    id long auto_increment,
    name varchar(14),
    WMO varchar(5),
    air_temp double,
    wind_speed double,
    weather_phenomenon varchar(255),
    timestamp datetime
);