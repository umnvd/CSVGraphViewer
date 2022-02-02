package com.umnvd.sensetestapp.models;

import androidx.annotation.NonNull;

import java.util.Objects;

public class DataPoint implements Comparable<DataPoint> {

    public final int x;
    public final int y;

    public DataPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int compareTo(@NonNull DataPoint other) {
        if (this.x != other.x) return this.x - other.x;
        return this.y - other.y;
    }

}
