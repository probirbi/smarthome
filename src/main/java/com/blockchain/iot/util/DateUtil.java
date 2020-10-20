package com.blockchain.iot.util;

public class DateUtil {

    public static boolean lessThanOneHour(long timeStamp) {
        long sixtyMinutes = System.currentTimeMillis() - 2 * 60 * 1000;
        if (timeStamp < sixtyMinutes) {
            return true;
        }
        return false;
    }
}
