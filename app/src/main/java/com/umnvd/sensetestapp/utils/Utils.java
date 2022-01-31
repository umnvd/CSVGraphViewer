package com.umnvd.sensetestapp.utils;

public class Utils {

    public static float coerceIn(float minValue, float value,float maxValue) {
        return Math.max(Math.min(maxValue, value), minValue);
    }

    public static float coerceAtMost(float maxValue, float value) {
        return Math.min(maxValue, value);
    }

}
