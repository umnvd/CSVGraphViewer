package com.umnvd.sensetestapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.umnvd.sensetestapp.data.TestPointsGenerator;
import com.umnvd.sensetestapp.views.plot.PlotView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TestPointsGenerator generator = new TestPointsGenerator();
        PlotView plotView = findViewById(R.id.plotView);
        plotView.setPoints(generator.generatePoints());
    }
}