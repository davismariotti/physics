package com.davismariotti.physics.constraints;

/**
 * Metrics for tracking collision system behavior
 * Useful for debugging clustering, energy pumping, and performance issues
 */
public class CollisionMetrics {
    private int totalCollisions;
    private int restingContacts;
    private int impulseApplications;
    private double maxPenetration;
    private int clusteredBalls;

    public void reset() {
        totalCollisions = 0;
        restingContacts = 0;
        impulseApplications = 0;
        maxPenetration = 0.0;
        clusteredBalls = 0;
    }

    public void recordCollision(double penetration, boolean appliedImpulse) {
        totalCollisions++;
        if (appliedImpulse) {
            impulseApplications++;
        } else {
            restingContacts++;
        }
        maxPenetration = Math.max(maxPenetration, penetration);
    }

    public void recordClusteredBalls(int count) {
        clusteredBalls = Math.max(clusteredBalls, count);
    }

    // Getters
    public int getTotalCollisions() {
        return totalCollisions;
    }

    public int getRestingContacts() {
        return restingContacts;
    }

    public int getImpulseApplications() {
        return impulseApplications;
    }

    public double getMaxPenetration() {
        return maxPenetration;
    }

    public int getClusteredBalls() {
        return clusteredBalls;
    }
}
