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
 * Test that horizontal momentum is preserved during bouncing
 */
class HorizontalMomentumTest {

    @Test
    void testBouncingBallKeepsHorizontalMomentum() {
        // Ball bouncing with high restitution should maintain horizontal velocity
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

        // Ball with high restitution, moving horizontally and falling
        MaterialProperties bouncyMaterial = new MaterialProperties(0.9, 0.0, 0.3, 0.2);
        Ball ball = new Ball(
                new Vector(20, 30),
                new Vector(15, 0),  // Moving right at 15 units/s
                Collections.emptyList(),
                bouncyMaterial
        );
        simulator.addBody(ball);

        DynamicBody ballDynamic = (DynamicBody) ball;
        double initialSpeed = Math.abs(ballDynamic.getVelocity().x());

        System.out.println("\n=== Horizontal Momentum During Bouncing Test ===");
        System.out.printf("Initial horizontal speed: %.2f%n", initialSpeed);

        // Simulate and check horizontal velocity after bounces
        double timestep = 1.0 / 60.0;
        int bounceCount = 0;
        boolean wasFalling = false;

        for (int i = 0; i < 600; i++) {
            simulator.update(timestep);

            double currentVelY = ballDynamic.getVelocity().y();
            double currentSpeed = Math.abs(ballDynamic.getVelocity().x());
            boolean isFalling = currentVelY < -0.1;
            boolean isRising = currentVelY > 0.1;

            if (wasFalling && isRising) {
                bounceCount++;
                System.out.printf("After bounce #%d: speed=%.2f (%.1f%% of initial)%n",
                        bounceCount, currentSpeed, (currentSpeed / initialSpeed) * 100);

                // Horizontal speed should be mostly preserved (>90% after each bounce)
                assertTrue(currentSpeed > initialSpeed * 0.9,
                        String.format("Bounce #%d: Horizontal speed lost too much. Initial: %.2f, Current: %.2f",
                                bounceCount, initialSpeed, currentSpeed));

                if (bounceCount >= 3) {
                    break;  // Test 3 bounces
                }
            }

            wasFalling = isFalling;
        }

        assertTrue(bounceCount >= 3, "Should have at least 3 bounces");
        System.out.println("✓ Horizontal momentum preserved during bouncing");
    }

    @Test
    void testSlidingBallLosesHorizontalMomentum() {
        // Ball with low restitution should lose horizontal momentum through friction
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

        // Ball with low restitution and high friction
        MaterialProperties slidingMaterial = new MaterialProperties(0.1, 0.0, 0.8, 0.6);
        Ball ball = new Ball(
                new Vector(20, 8),  // Low height so it doesn't bounce much
                new Vector(15, 0),
                Collections.emptyList(),
                slidingMaterial
        );
        simulator.addBody(ball);

        DynamicBody ballDynamic = (DynamicBody) ball;
        double initialVelX = ballDynamic.getVelocity().x();

        System.out.println("\n=== Sliding Friction Test ===");
        System.out.printf("Initial horizontal velocity: %.2f%n", initialVelX);

        // Simulate
        double timestep = 1.0 / 60.0;
        for (int i = 0; i < 180; i++) {  // 3 seconds
            simulator.update(timestep);
        }

        double finalVelX = ballDynamic.getVelocity().x();
        System.out.printf("Final horizontal velocity: %.2f (%.1f%% of initial)%n",
                finalVelX, (finalVelX / initialVelX) * 100);

        // Friction should have significantly reduced horizontal velocity
        assertTrue(finalVelX < initialVelX * 0.7,
                String.format("Sliding friction should reduce velocity. Initial: %.2f, Final: %.2f",
                        initialVelX, finalVelX));
        System.out.println("✓ Friction correctly reduces sliding velocity");
    }
}
