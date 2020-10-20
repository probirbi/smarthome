package com.blockchain.iot.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnergyConsumption {

    private String timeStamp;
    private double totalElectricity;
    private double consumedElectricity;
    private double wastedElectricity;
}
