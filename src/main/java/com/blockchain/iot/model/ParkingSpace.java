package com.blockchain.iot.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ParkingSpace {

    private String timestamp;
    private int totalSpace;
    private int parkedSpace;
    private int freeSpace;
    private String hash;
}
