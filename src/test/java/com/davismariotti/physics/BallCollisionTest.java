package com.davismariotti.physics;

import com.davismariotti.physics.constraints.BoundaryConstraint;
import com.davismariotti.physics.core.PhysicsConfig;
import com.davismariotti.physics.core.PhysicsSimulator;
import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.sprites.Ball;
import com.davismariotti.physics.sprites.DynamicBody;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ball-to-ball collision detection and resolution
 */
class BallCollisionTest {

    @Test
    void testHeadOnCollisionVelocityExchange() {
        // Equal mass elastic collision: moving ball hits stationary ball
        // Expected: velocities should exchange (Newton's cradle effect)
        PhysicsConfig config = new PhysicsConfig();
        PhysicsSimulator simulator = new PhysicsSimulator(config);

        // No boundaries or gravity for this test
        config.setGravity(new Vector(0, 0));

        // Ball A: moving right at 10 units/sec
        Ball ballA = new Ball(
                new Vector(20, 50),
                new Vector(10, 0),
                Collections.emptyList(),
                1.0,  // Perfect restitution
                0.0   // No drag
        );

        // Ball B: stationary, positioned so collision happens soon
        // Ball radius is 5, so they collide when centers are 10 units apart
        Ball ballB = new Ball(
                new Vector(50, 50),  // 30 units away, will collide in 3 seconds
                Vector.ZERO,
                Collections.emptyList(),
                1.0,
                0.0
        );

        simulator.addBody(ballA);
        simulator.addBody(ballB);

        DynamicBody ballADynamic = (DynamicBody) ballA;
        DynamicBody ballBDynamic = (DynamicBody) ballB;

        System.out.println("\n=== Head-On Collision Test ===");
        System.out.printf("Initial: A_vel=(%.2f, %.2f), B_vel=(%.2f, %.2f)%n",
                ballADynamic.getVelocity().x(), ballADynamic.getVelocity().y(),
                ballBDynamic.getVelocity().x(), ballBDynamic.getVelocity().y());

        // Simulate until collision
        double timestep = 1.0 / 60.0;
        double minDistance = Double.MAX_VALUE;
        boolean collisionDetected = false;

        for (int i = 0; i < 300; i++) {
            double prevMinDist = minDistance;
            simulator.update(timestep);

            double dx = ballBDynamic.getPosition().x() - ballADynamic.getPosition().x();
            double dy = ballBDynamic.getPosition().y() - ballADynamic.getPosition().y();
            double distance = Math.sqrt(dx * dx + dy * dy);

            minDistance = Math.min(minDistance, distance);

            // Collision happened when distance was at minimum and now increasing
            if (distance > prevMinDist + 0.1 && minDistance < 11.0) {
                collisionDetected = true;
                System.out.printf("Collision detected at frame %d, min distance: %.3f%n", i, minDistance);
                System.out.printf("After: A_vel=(%.2f, %.2f), B_vel=(%.2f, %.2f)%n",
                        ballADynamic.getVelocity().x(), ballADynamic.getVelocity().y(),
                        ballBDynamic.getVelocity().x(), ballBDynamic.getVelocity().y());
                break;
            }
        }

        assertTrue(collisionDetected, "Collision should have been detected");

        // For equal mass elastic collision: velocities exchange
        // Ball A should stop (or nearly stop), Ball B should move at ~10
        double velA = ballADynamic.getVelocity().x();
        double velB = ballBDynamic.getVelocity().x();

        System.out.printf("Expected: A_vel ≈ 0, B_vel ≈ 10%n");
        System.out.printf("Actual: A_vel = %.3f, B_vel = %.3f%n", velA, velB);

        assertTrue(Math.abs(velA) < 2.0, "Ball A should nearly stop after collision");
        assertTrue(velB > 8.0, "Ball B should move forward after being hit");
    }

    @Test
    void testMomentumConservation() {
        // Two balls collide - total momentum must be conserved
        PhysicsConfig config = new PhysicsConfig();
        config.setGravity(new Vector(0, 0));
        PhysicsSimulator simulator = new PhysicsSimulator(config);

        // Ball A: moving right
        Ball ballA = new Ball(
                new Vector(30, 50),
                new Vector(15, 0),
                Collections.emptyList(),
                1.0,
                0.0
        );

        // Ball B: moving left
        Ball ballB = new Ball(
                new Vector(70, 50),
                new Vector(-10, 0),
                Collections.emptyList(),
                1.0,
                0.0
        );

        simulator.addBody(ballA);
        simulator.addBody(ballB);

        DynamicBody ballADynamic = (DynamicBody) ballA;
        DynamicBody ballBDynamic = (DynamicBody) ballB;

        // Calculate initial momentum
        double mA = ballADynamic.getMass();
        double mB = ballBDynamic.getMass();
        double initialMomentum = mA * ballADynamic.getVelocity().x() + mB * ballBDynamic.getVelocity().x();

        System.out.println("\n=== Momentum Conservation Test ===");
        System.out.printf("Initial momentum: %.3f%n", initialMomentum);

        // Simulate collision
        double timestep = 1.0 / 60.0;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < 200; i++) {
            double prevMinDist = minDistance;
            simulator.update(timestep);

            double dx = ballBDynamic.getPosition().x() - ballADynamic.getPosition().x();
            double distance = Math.abs(dx);
            minDistance = Math.min(minDistance, distance);

            if (distance > prevMinDist + 0.1 && minDistance < 11.0) {
                System.out.printf("Collision at frame %d%n", i);
                break;
            }
        }

        // Check momentum after collision
        double finalMomentum = mA * ballADynamic.getVelocity().x() + mB * ballBDynamic.getVelocity().x();

        System.out.printf("Final momentum: %.3f%n", finalMomentum);
        System.out.printf("Momentum error: %.6f%n", Math.abs(finalMomentum - initialMomentum));

        double momentumError = Math.abs(finalMomentum - initialMomentum);
        assertTrue(momentumError < 0.1,
                String.format("Momentum not conserved. Initial: %.3f, Final: %.3f",
                        initialMomentum, finalMomentum));
    }

    @Test
    void testEnergyConservation() {
        // Elastic collision should conserve kinetic energy
        PhysicsConfig config = new PhysicsConfig();
        config.setGravity(new Vector(0, 0));
        PhysicsSimulator simulator = new PhysicsSimulator(config);

        Ball ballA = new Ball(
                new Vector(30, 50),
                new Vector(20, 0),
                Collections.emptyList(),
                1.0,
                0.0
        );

        Ball ballB = new Ball(
                new Vector(70, 50),
                Vector.ZERO,
                Collections.emptyList(),
                1.0,
                0.0
        );

        simulator.addBody(ballA);
        simulator.addBody(ballB);

        DynamicBody ballADynamic = (DynamicBody) ballA;
        DynamicBody ballBDynamic = (DynamicBody) ballB;

        // Calculate initial kinetic energy
        double mA = ballADynamic.getMass();
        double mB = ballBDynamic.getMass();
        double vA0 = ballADynamic.getVelocity().getMagnitude();
        double vB0 = ballBDynamic.getVelocity().getMagnitude();
        double initialKE = 0.5 * mA * vA0 * vA0 + 0.5 * mB * vB0 * vB0;

        System.out.println("\n=== Energy Conservation Test ===");
        System.out.printf("Initial KE: %.3f%n", initialKE);

        // Simulate collision
        double timestep = 1.0 / 60.0;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < 200; i++) {
            double prevMinDist = minDistance;
            simulator.update(timestep);

            double dx = ballBDynamic.getPosition().x() - ballADynamic.getPosition().x();
            double distance = Math.abs(dx);
            minDistance = Math.min(minDistance, distance);

            if (distance > prevMinDist + 0.1 && minDistance < 11.0) {
                System.out.printf("Collision at frame %d%n", i);
                break;
            }
        }

        // Check kinetic energy after collision
        double vA = ballADynamic.getVelocity().getMagnitude();
        double vB = ballBDynamic.getVelocity().getMagnitude();
        double finalKE = 0.5 * mA * vA * vA + 0.5 * mB * vB * vB;

        System.out.printf("Final KE: %.3f%n", finalKE);
        System.out.printf("Energy error: %.3f (%.2f%%)%n",
                Math.abs(finalKE - initialKE),
                (Math.abs(finalKE - initialKE) / initialKE) * 100);

        double energyError = Math.abs(finalKE - initialKE);
        assertTrue(energyError < 1.0,
                String.format("Energy not conserved. Initial: %.3f, Final: %.3f",
                        initialKE, finalKE));
    }
}
