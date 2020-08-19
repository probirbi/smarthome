package com.blockchain.iot.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SmartHome {

    private String timestamp;
    private int smokeDetectors;
    private int doorLocks;
    private int windows;
    private int homeAppliances;
    private int lightBulbs;
}
