package com.blockchain.iot.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Sensor {

    private String timestamp;
    private double temperatureCelsius;
    private double temperatureFahrenheit;
    private double humidity;
}
