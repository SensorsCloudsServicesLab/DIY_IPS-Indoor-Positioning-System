package com.scslab.indoorpositioning;

import androidx.annotation.NonNull;

import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.Map;

public class RoomMatrix<T> {

    private final double positionDifference = 0.2;
    private final T[][] data;
    public int xArrayLength;
    public int yArrayLength;

    public RoomMatrix(Map<Position, T> data, Class<? extends T> TClass) {
        double xMax = 0;
        double yMax = 0;
        for (Position position : data.keySet()) {
            if (position.x > xMax) {
                xMax = position.x;
            }
            if (position.y > yMax) {
                yMax = position.y;
            }
        }

        xArrayLength = 1 + (int) (xMax / positionDifference);
        yArrayLength = 1 + (int) (yMax / positionDifference);

        this.data = (T[][]) Array.newInstance(TClass, yArrayLength, xArrayLength);
        for (Position position : data.keySet()) {
            this.data[positionToYIndex(position)][positionToXIndex(position)] = data.get(position);
        }
    }

    public RoomMatrix(T[][] data) {
        this.data = data;
        this.yArrayLength = data.length;
        this.xArrayLength = data[0].length;
    }

    private int positionToXIndex(Position position) {
        return (int) Math.round(position.x / positionDifference);
    }

    private int positionToYIndex(Position position) {
        return (int) Math.round(position.y / positionDifference);
    }

    public T getValueAtPosition(Position position) {
        return this.data[positionToYIndex(position)][positionToXIndex(position)];
    }

    public T getValueAtIndex(int row, int col) {
        return this.data[row][col];
    }

    public T getMaxValue(Comparator<T> comparator) {
        T maxValue = data[0][0];

        for (int row = 0; row < yArrayLength; row++) {
            for (int col = 0; col < xArrayLength; col++) {
                if (comparator.compare(data[row][col], maxValue) > 0) {
                    maxValue = data[row][col];
                }
            }
        }

        return maxValue;
    }

    public RoomMatrix<T> operate(RoomMatrixUnitOperator<T> operator) {
        for (int row = 0; row < yArrayLength; row++) {
            for (int col = 0; col < xArrayLength; col++) {
                data[row][col] = operator.operate(row, col);
            }
        }
        return this;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        for (int row = 0; row < yArrayLength; row++) {
            for (int col = 0; col < xArrayLength; col++) {
                string.append(getValueAtIndex(row, col));
            }
            string.append("\n");
        }
        return string.toString();
    }

    @FunctionalInterface
    public interface RoomMatrixUnitOperator<T> {
        T operate(int row, int col);
    }

}
