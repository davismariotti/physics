package com.davismariotti.physics;

import com.davismariotti.physics.constraints.BoundaryConstraint;
import com.davismariotti.physics.constraints.ContinuousCollisionConstraint;
import com.davismariotti.physics.core.PhysicsConfig;
import com.davismariotti.physics.core.PhysicsSimulator;
import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.sprites.Ball;
import com.davismariotti.physics.sprites.DynamicBody;
import com.davismariotti.physics.sprites.Ground;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify energy conservation with continuous collision detection
 *
 * With perfect restitution (1.0) and no drag (0.0), a ball should bounce
 * to the same height indefinitely, demonstrating energy conservation.
 */
class EnergyConservationTest {

    @Test
    void testEnergyConservationWithPerfectRestitution() {
        // Test configuration
        double initialHeight = 50.0;
        double tolerance = 0.5;  // Allow 0.5 units error due to numerical precision
        int numBounces = 5;

        // Create physics simulation with perfect conditions
        PhysicsConfig config = new PhysicsConfig();
        PhysicsSimulator simulator = new PhysicsSimulator(config);

        // Create ground at y=0
        Ground ground = new Ground(50, 2.5, 100, 5);
        simulator.addBody(ground);

        // Add continuous collision constraint
        simulator.addConstraint(new ContinuousCollisionConstraint(simulator.getStaticBodies(), config.getGravity()));

        // Add boundary constraint
        simulator.addConstraint(new BoundaryConstraint(0, 100, 0, 100));

        // Create ball with perfect restitution and no drag
        Ball ball = new Ball(
                new Vector(50, initialHeight),
                Vector.ZERO,
                Collections.emptyList(),
                1.0,  // Perfect restitution
                0.0   // No drag
        );
        simulator.addBody(ball);

        // Simulate and track bounce heights
        double timestep = 1.0 / 60.0;
        int maxIterations = 10000;
        int bounceCount = 0;
        boolean wasFalling = false;

        for (int i = 0; i < maxIterations && bounceCount < numBounces; i++) {
            simulator.update(timestep);

            DynamicBody ballDynamic = (DynamicBody) ball;
            double currentVelY = ballDynamic.getVelocity().y();

            boolean isFalling = currentVelY < -0.1;
            boolean isRising = currentVelY > 0.1;

            if (wasFalling && isRising) {
                // Just bounced!
                bounceCount++;

                // Find peak height
                double peakY = findPeakHeight(simulator, ballDynamic, timestep);
                double error = Math.abs(peakY - initialHeight);

                System.out.printf("Bounce #%d: Peak height = %.3f (error: %.3f units, %.2f%%)%n",
                        bounceCount, peakY, error, (error / initialHeight) * 100.0);

                // Assert energy is conserved within tolerance
                assertTrue(error <= tolerance,
                        String.format("Bounce #%d: Energy not conserved. Expected height: %.3f, actual: %.3f, error: %.3f",
                                bounceCount, initialHeight, peakY, error));
            }

            wasFalling = isFalling;
        }

        // Verify we completed all bounces
        assertEquals(numBounces, bounceCount, "Should have completed " + numBounces + " bounces");
    }

    @Test
    void testEnergyLossWithImperfectRestitution() {
        // With restitution < 1.0, energy should decrease
        double initialHeight = 50.0;
        double restitution = 0.8;

        PhysicsConfig config = new PhysicsConfig();
        PhysicsSimulator simulator = new PhysicsSimulator(config);

        Ground ground = new Ground(50, 2.5, 100, 5);
        simulator.addBody(ground);

        simulator.addConstraint(new ContinuousCollisionConstraint(simulator.getStaticBodies(), config.getGravity()));
        simulator.addConstraint(new BoundaryConstraint(0, 100, 0, 100));

        Ball ball = new Ball(
                new Vector(50, initialHeight),
                Vector.ZERO,
                Collections.emptyList(),
                restitution,
                0.0
        );
        simulator.addBody(ball);

        // Simulate first bounce
        double timestep = 1.0 / 60.0;
        int maxIterations = 5000;
        boolean wasFalling = false;

        for (int i = 0; i < maxIterations; i++) {
            simulator.update(timestep);

            DynamicBody ballDynamic = (DynamicBody) ball;
            double currentVelY = ballDynamic.getVelocity().y();

            boolean isFalling = currentVelY < -0.1;
            boolean isRising = currentVelY > 0.1;

            if (wasFalling && isRising) {
                // Found first bounce
                double peakY = findPeakHeight(simulator, ballDynamic, timestep);

                System.out.printf("First bounce with restitution=%.1f: Peak = %.3f (%.1f%% of original)%n",
                        restitution, peakY, (peakY / initialHeight) * 100.0);

                // Ball should bounce lower with imperfect restitution
                assertTrue(peakY < initialHeight,
                        "Ball should not bounce as high with imperfect restitution");

                // Should be roughly restitution^2 of original height (potential energy scales with height)
                double expectedRatio = restitution * restitution;
                double actualRatio = peakY / initialHeight;
                double ratioError = Math.abs(expectedRatio - actualRatio);

                assertTrue(ratioError < 0.1,
                        String.format("Energy loss should follow restitution coefficient. Expected ratio: %.3f, actual: %.3f",
                                expectedRatio, actualRatio));
                break;
            }

            wasFalling = isFalling;
        }
    }

    /**
     * Helper method to find peak height after a bounce
     */
    private double findPeakHeight(PhysicsSimulator simulator, DynamicBody ball, double timestep) {
        double maxY = ball.getPosition().y();
        double lastVelY = ball.getVelocity().y();

        // Simulate until velocity becomes negative (ball starts falling)
        for (int i = 0; i < 1000; i++) {
            simulator.update(timestep);
            double currentY = ball.getPosition().y();
            double currentVelY = ball.getVelocity().y();

            if (currentY > maxY) {
                maxY = currentY;
            }

            // Ball reached peak when velocity changes from positive to negative
            if (lastVelY > 0 && currentVelY < 0) {
                return maxY;
            }

            lastVelY = currentVelY;
        }

        return maxY;
    }

}
