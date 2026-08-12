package com.nf.arabmoviehubapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class EmulatorDetector {

    private final Context context;

    public EmulatorDetector(Context context) {
        this.context = context.getApplicationContext();
    }

    public boolean isEmulator() {
        return isEmulatorByBuildProps() || hasEmulatorCharacteristicFiles() || hasLowSensorCount() || isQemuEmulator();
    }

    private boolean hasEmulatorCharacteristicFiles() {
        String[] files = {"/dev/qemu_pipe", "/dev/socket/qemud", "/dev/socket/genyd", "/system/lib/libc_malloc_debug_qemu.so", "/sys/qemu_trace", "/system/bin/qemu-props", "/dev/socket/baseband_genyd"};
        for (String filePath : files) {
            try {
                if (new File(filePath).exists()) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    private boolean hasLowSensorCount() {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager == null) {
            return false;
        }
        return sensorManager.getSensorList(Sensor.TYPE_ALL).size() <= 7;
    }

    private boolean isEmulatorByBuildProps() {
        String product = safeLower(Build.PRODUCT);
        String model = safeLower(Build.MODEL);
        String brand = safeLower(Build.BRAND);
        String device = safeLower(Build.DEVICE);
        String manufacturer = safeLower(Build.MANUFACTURER);
        String hardware = safeLower(Build.HARDWARE);
        String fingerprint = safeLower(Build.FINGERPRINT);

        if (product.contains("sdk") || product.contains("google_sdk") || product.contains("sdk_x86") || product.contains("sdk_gphone") || product.contains("vbox") || model.contains("emulator") || model.contains("android sdk built for x86") || model.contains("android sdk built for x64")) {
            return true;
        }

        if (brand.startsWith("generic") && device.startsWith("generic")) {
            return true;
        }

        if (manufacturer.contains("genymotion")) {
            return true;
        }

        if (hardware.contains("goldfish") || hardware.contains("ranchu") || hardware.contains("qemu")) {
            return true;
        }

        if (fingerprint.startsWith("generic") || fingerprint.contains("test-keys")) {
            return true;
        }

        return false;
    }

    private boolean isQemuEmulator() {

        Process process = null;
        try {
            process = Runtime.getRuntime().exec("getprop ro.kernel.qemu");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String value = reader.readLine();
            return "1".equals(value);
        } catch (Exception ignored) {
            return false;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private String safeLower(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase();
    }
}