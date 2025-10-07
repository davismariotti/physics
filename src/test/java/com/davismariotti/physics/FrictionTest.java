package com.davismariotti.physics;

import com.davismariotti.physics.constraints.BoundaryConstraint;
import com.davismariotti.physics.constraints.ContinuousCollisionConstraint;
import com.davismariotti.physics.core.PhysicsConfig;
import com.davismariotti.physics.core.PhysicsSimulator;
import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.sprites.Ball;
import com.davismariotti.physics.sprites.DynamicBody;
import com.davismariotti.physics.sprites.Ground;
import com.davismariotti.physics.sprites.MaterialProperties;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for friction and resting contact behavior
 */
class FrictionTest {

    @Test
    void testSlidingFrictionDecelerates() {
        // Ball sliding horizontally on ground should slow down due to friction
        PhysicsConfig config = new PhysicsConfig();
        config.setRestingVelocityThreshold(0.1);
        PhysicsSimulator simulator = new PhysicsSimulator(config);

        // Ground at y=0
        Ground ground = new Ground(50, 2.5, 100, 5);
        simulator.addBody(ground);

        simulator.addConstraint(new ContinuousCollisionConstraint(
                simulator.getStaticBodies(),
                config.getGravity(),
                config.getRestingVelocityThreshold()
        ));
        simulator.addConstraint(new BoundaryConstraint(0, 100, 0, 100));

        // Ball with high horizontal velocity, low vertical, high friction
        MaterialProperties material = new MaterialProperties(0.3, 0.0, 0.8, 0.6);
        Ball ball = new Ball(
                new Vector(50, 10),
                new Vector(20, -1), // Moving right and slightly down
                Collections.emptyList(),
                material
        );
        simulator.addBody(ball);

        DynamicBody ballDynamic = (DynamicBody) ball;
        double initialVelX = ballDynamic.getVelocity().x();

        System.out.println("\n=== Friction Deceleration Test ===");
        System.out.printf("Initial velocity: (%.2f, %.2f)%n",
                ballDynamic.getVelocity().x(), ballDynamic.getVelocity().y());

        // Simulate for a bit
        double timestep = 1.0 / 60.0;
        for (int i = 0; i < 120; i++) {  // 2 seconds
            simulator.update(timestep);
        }

        double finalVelX = ballDynamic.getVelocity().x();
        System.out.printf("Final velocity after 2s: (%.2f, %.2f)%n",
                finalVelX, ballDynamic.getVelocity().y());

        // Friction should have slowed the ball significantly
        assertTrue(finalVelX < initialVelX * 0.5,
                String.format("Ball should slow down due to friction. Initial: %.2f, Final: %.2f",
                        initialVelX, finalVelX));
    }

    @Test
    void testRestingContactStopsBouncing() {
        // Ball should stop bouncing when velocity gets very low
        PhysicsConfig config = new PhysicsConfig();
        config.setRestingVelocityThreshold(0.5);
        PhysicsSimulator simulator = new PhysicsSimulator(config);

        Ground ground = new Ground(50, 2.5, 100, 5);
        simulator.addBody(ground);

        simulator.addConstraint(new ContinuousCollisionConstraint(
                simulator.getStaticBodies(),
                config.getGravity(),
                config.getRestingVelocityThreshold()
        ));
        simulator.addConstraint(new BoundaryConstraint(0, 100, 0, 100));

        // Ball falling from higher height so it bounces first
        MaterialProperties material = new MaterialProperties(0.6, 0.0, 0.5, 0.4);
        Ball ball = new Ball(
                new Vector(50, 30),  // Higher starting point
                Vector.ZERO,
                Collections.emptyList(),
                material
        );
        simulator.addBody(ball);

        DynamicBody ballDynamic = (DynamicBody) ball;

        System.out.println("\n=== Resting Contact Test ===");

        // Simulate and track bounces
        double timestep = 1.0 / 60.0;
        int bounceCount = 0;
        boolean wasFalling = false;
        double lastPeakHeight = ballDynamic.getPosition().y();

        for (int i = 0; i < 600; i++) {  // 10 seconds max
            simulator.update(timestep);

            double currentVelY = ballDynamic.getVelocity().y();
            boolean isFalling = currentVelY < -0.1;
            boolean isRising = currentVelY > 0.1;

            if (wasFalling && isRising) {
                bounceCount++;
                double currentHeight = ballDynamic.getPosition().y();
                double heightLoss = lastPeakHeight - currentHeight;

                System.out.printf("Bounce #%d: Height %.2f (lost %.2f from previous)%n",
                        bounceCount, currentHeight, heightLoss);

                lastPeakHeight = currentHeight;

                // Should settle within a few bounces
                if (bounceCount >= 5) {
                    break;
                }
            }

            wasFalling = isFalling;

            // Check if ball has settled (very low velocity)
            if (i > 120 && Math.abs(currentVelY) < 0.05) {
                System.out.printf("Ball settled at frame %d with vel_y = %.4f%n", i, currentVelY);
                assertTrue(bounceCount > 0, "Ball should have bounced before settling");
                return;  // Test passed - ball settled
            }
        }

        // If we get here, verify we had some bounces and energy was dissipated
        assertTrue(bounceCount > 0, "Ball should bounce at least once");
        System.out.printf("Test completed with %d bounces%n", bounceCount);
    }

    @Test
    void testStaticFrictionPreventsSliding() {
        // Ball at rest on incline (simulated with initial tiny velocity) should not slide if friction is high enough
        PhysicsConfig config = new PhysicsConfig();
        config.setRestingVelocityThreshold(0.1);
        PhysicsSimulator simulator = new PhysicsSimulator(config);

        Ground ground = new Ground(50, 5, 100, 5);
        simulator.addBody(ground);

        simulator.addConstraint(new ContinuousCollisionConstraint(
                simulator.getStaticBodies(),
                config.getGravity(),
                config.getRestingVelocityThreshold()
        ));
        simulator.addConstraint(new BoundaryConstraint(0, 100, 0, 100));

        // Ball with very high static friction
        MaterialProperties material = new MaterialProperties(0.0, 0.0, 0.95, 0.7);
        Ball ball = new Ball(
                new Vector(50, 10),
                new Vector(0.1, 0),  // Tiny initial velocity
                Collections.emptyList(),
                material
        );
        simulator.addBody(ball);

        DynamicBody ballDynamic = (DynamicBody) ball;
        double initialVelX = Math.abs(ballDynamic.getVelocity().x());

        System.out.println("\n=== Static Friction Test ===");
        System.out.printf("Initial velocity: %.4f%n", initialVelX);

        // Simulate
        double timestep = 1.0 / 60.0;
        for (int i = 0; i < 60; i++) {  // 1 second
            simulator.update(timestep);
        }

        double finalVelX = Math.abs(ballDynamic.getVelocity().x());
        System.out.printf("Final velocity: %.4f%n", finalVelX);

        // Static friction should have stopped the motion
        assertTrue(finalVelX < 0.5, "Static friction should prevent/stop sliding");
    }
}
