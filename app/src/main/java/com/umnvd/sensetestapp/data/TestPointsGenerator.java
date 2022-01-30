package com.umnvd.sensetestapp.data;

import com.umnvd.sensetestapp.models.DataPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestPointsGenerator {

    private final Random random = new Random();

    public List<DataPoint> generatePoints() {
        List<DataPoint> result = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            result.add(new DataPoint(
                    random.nextInt(10) - 5,
                    random.nextInt(20) - 10
//                    i, i
            ));
        }
        return result;
    }
}
