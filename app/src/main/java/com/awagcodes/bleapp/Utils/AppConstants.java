package com.awagcodes.bleapp.Utils;

import java.util.UUID;

public class AppConstants {

    public static UUID HEART_RATE_SERVICE_UUID = convertFromInteger(0x180D);
    public static UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = convertFromInteger(0x2902);
    public static UUID BODY_SENSOR_LOCATION_UUID = convertFromInteger(0x2A38);
    public static UUID HEART_RATE_MEASUREMENT_UUID = convertFromInteger(0x2A37);

    private static UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

}
