package com.example.smartdrill.routing;

/**
 * Part 2: TimeEstimator
 * Calculates how long it will take a person to walk a certain distance.
 * Assumes standard walking speed during evacuation (approx. 1.4 meters per second or ~5 km/h).
 */
public class TimeEstimator {

    // Average human walking speed during emergencies (meters per second)
    private static final double WALKING_SPEED_MPS = 1.4;

    public static String estimateArrivalTime(double distanceMeters) {
        if (distanceMeters <= 0) return "Arriving immediately";
        double seconds = distanceMeters / WALKING_SPEED_MPS;
        if (seconds < 60) {
            return String.format("About %d seconds", (int) seconds);
        } else {
            int minutes = (int) (seconds / 60);
            int remainingSecs = (int) (seconds % 60);
            if (remainingSecs > 30) minutes++;
            return String.format("About %d minute%s", minutes, minutes == 1 ? "" : "s");
        }
    }
}
