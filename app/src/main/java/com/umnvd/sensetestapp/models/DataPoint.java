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

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        DataPoint dataPoint = (DataPoint) o;
//        return x == dataPoint.x && y == dataPoint.y;
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(x, y);
//    }

}
