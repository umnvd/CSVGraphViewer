package com.umnvd.sensetestapp.data;

import com.umnvd.sensetestapp.models.DataPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestPointsGenerator {

    private final Random random = new Random();

    public List<DataPoint> generatePoints() {
        return quad(-5, 5);
//        return pow(-25, 25, 3);
//        return sin(0, 100);
    }

    public List<DataPoint> quad(int minX, int maxX) {
        List<DataPoint> result = new ArrayList<>();
        for (int i = minX; i <= maxX; i++) {
            result.add(new DataPoint(i, i*i));
        }
        return result;
    }

    public List<DataPoint> pow(int minX, int maxX, int exp) {
        List<DataPoint> result = new ArrayList<>();
        for (int i = minX; i <= maxX; i++) {
            result.add(new DataPoint(i, (int) Math.pow(i, exp)));
        }
        return result;
    }

    public List<DataPoint> sin(int minX, int maxX) {
        List<DataPoint> result = new ArrayList<>();
        for (int i = minX; i <= maxX; i++) {
            result.add(new DataPoint(i, (int) (Math.sin(i) * 1000)));
        }
        return result;
    }
}
