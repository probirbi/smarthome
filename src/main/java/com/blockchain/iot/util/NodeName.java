package com.blockchain.iot.util;

public class NodeName {

    public static String getNodeName(Integer node) {
        switch (node) {
            case 1: return "TemperatureNode";

            case 2: return "SmartHomeNode";

            case 3: return "ParkingSpaceNode";

                case 5: return "EHealthNode";

                case 8: return "EnergyConsumptionNode";
            default: return "";
        }
    }
}
