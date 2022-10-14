package pe.saydomonkos.lightweightgps;

import android.os.Bundle;

public class HistoryItem {

    private double[] velocities;
    private double remainingDistance;
    private double velocityOnRemainingDistance;
    private double averageVelocity;
    private double totalDistance;

    public HistoryItem() {
        velocities = new double[0];
        averageVelocity = 0;
        totalDistance = 0;
    }

    public HistoryItem velocities(double[] velocities) {
        this.velocities = velocities;
        return this;
    }

    public HistoryItem remainingDistance(double remainingDistance) {
        this.remainingDistance = remainingDistance;
        return this;
    }

    public HistoryItem velocityOnRemainingDistance(double velocityOnRemainingDistance) {
        this.velocityOnRemainingDistance = velocityOnRemainingDistance;
        return this;
    }

    public HistoryItem averageVelocity(double averageVelocity) {
        this.averageVelocity = averageVelocity;
        return this;
    }

    public HistoryItem totalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
        return this;
    }

    public double[] getVelocities() {
        return velocities;
    }

    public double getRemainingDistance() {
        return remainingDistance;
    }

    public double getVelocityOnRemainingDistance() {
        return velocityOnRemainingDistance;
    }

    public double getAverageVelocity() {
        return averageVelocity;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public Bundle getHistoryItemBundled() {
        Bundle historyItemBundle = new Bundle();
        historyItemBundle.putDoubleArray("velocities", velocities);
        historyItemBundle.putDouble("remainingDistance", remainingDistance);
        historyItemBundle.putDouble("velocityOnRemainingDistance", velocityOnRemainingDistance);
        historyItemBundle.putDouble("averageVelocity", averageVelocity);
        historyItemBundle.putDouble("totalDistance", totalDistance);
        return historyItemBundle;
    }
}
