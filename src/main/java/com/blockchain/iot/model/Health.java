package com.blockchain.iot.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Health {

    private String timeStamp;
    private int pulseRate;
    private int bloodPressure;
}
