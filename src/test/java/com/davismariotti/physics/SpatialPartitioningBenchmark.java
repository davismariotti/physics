package com.davismariotti.physics;

import com.davismariotti.physics.constraints.BoundaryConstraint;
import com.davismariotti.physics.constraints.ContinuousCollisionConstraint;
import com.davismariotti.physics.core.PhysicsConfig;
import com.davismariotti.physics.core.PhysicsSimulator;
import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.sprites.Ball;
import com.davismariotti.physics.sprites.Ground;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Performance benchmark comparing naive O(n²) vs spatial partitioning O(n) collision detection
 */
class SpatialPartitioningBenchmark {

    @Test
    void testBenchmark100Balls() {
        System.out.println("\n=== Benchmark: 100 Balls ===");
        runBenchmark(100, 100);
    }

    @Test
    void testBenchmark500Balls() {
        System.out.println("\n=== Benchmark: 500 Balls ===");
        runBenchmark(500, 50);
    }

    @Test
    void testBenchmark1000Balls() {
        System.out.println("\n=== Benchmark: 1000 Balls ===");
        runBenchmark(1000, 30);
    }

    @Test
    void testBenchmark2000Balls() {
        System.out.println("\n=== Benchmark: 2000 Balls ===");
        runBenchmark(2000, 20);
    }

    /**
     * Run benchmark with given number of balls
     */
    private void runBenchmark(int ballCount, int frameCount) {
        double worldWidth = 120.0;
        double worldHeight = 80.0;

        // Test WITHOUT spatial partitioning
        long timeWithout = benchmarkWithoutSpatialPartitioning(ballCount, frameCount, worldWidth, worldHeight);

        // Test WITH spatial partitioning
        long timeWith = benchmarkWithSpatialPartitioning(ballCount, frameCount, worldWidth, worldHeight);

        // Calculate speedup
        double speedup = (double) timeWithout / timeWith;

        System.out.printf("Without spatial partitioning: %d ms (%.2f ms/frame)%n",
                timeWithout, (double) timeWithout / frameCount);
        System.out.printf("With spatial partitioning:    %d ms (%.2f ms/frame)%n",
                timeWith, (double) timeWith / frameCount);
        System.out.printf("Speedup: %.2fx%n", speedup);

        // For > 100 balls, spatial partitioning should be faster
        if (ballCount > 100) {
            assertTrue(timeWith < timeWithout,
                    String.format("Spatial partitioning should be faster for %d balls (was %.2fx)",
                            ballCount, speedup));
        }
    }

    private long benchmarkWithoutSpatialPartitioning(int ballCount, int frameCount,
                                                     double worldWidth, double worldHeight) {
        PhysicsConfig config = new PhysicsConfig();
        config.setUseSpatialPartitioning(false);  // Disable spatial partitioning
        config.setSubsteps(1);  // Use 1 substep for fair comparison

        PhysicsSimulator simulator = createSimulator(config, ballCount, worldWidth, worldHeight);

        // Warmup
        for (int i = 0; i < 10; i++) {
            simulator.update(1.0 / 60.0);
        }

        // Benchmark
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < frameCount; i++) {
            simulator.update(1.0 / 60.0);
        }
        long endTime = System.currentTimeMillis();

        return endTime - startTime;
    }

    private long benchmarkWithSpatialPartitioning(int ballCount, int frameCount,
                                                  double worldWidth, double worldHeight) {
        PhysicsConfig config = new PhysicsConfig();
        config.setUseSpatialPartitioning(true);   // Enable spatial partitioning
        config.setGridCellSize(0.5);              // Optimal for ball radius 0.25
        config.setSubsteps(1);  // Use 1 substep for fair comparison

        PhysicsSimulator simulator = createSimulator(config, ballCount, worldWidth, worldHeight);
        simulator.setWorldBounds(0, worldWidth, 0, worldHeight);

        // Warmup
        for (int i = 0; i < 10; i++) {
            simulator.update(1.0 / 60.0);
        }

        // Benchmark
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < frameCount; i++) {
            simulator.update(1.0 / 60.0);
        }
        long endTime = System.currentTimeMillis();

        return endTime - startTime;
    }

    /**
     * Create simulator with specified number of balls distributed randomly
     */
    private PhysicsSimulator createSimulator(PhysicsConfig config, int ballCount,
                                            double worldWidth, double worldHeight) {
        PhysicsSimulator simulator = new PhysicsSimulator(config);

        // Add ground
        double groundHeight = worldHeight * 0.05;
        Ground ground = new Ground(
                worldWidth / 2,
                groundHeight / 2,
                worldWidth,
                groundHeight
        );
        simulator.addBody(ground);

        // Add boundary constraint
        BoundaryConstraint boundaryConstraint = new BoundaryConstraint(
                0, worldWidth,
                0, worldHeight
        );
        simulator.addConstraint(boundaryConstraint);

        // Add continuous collision constraint
        simulator.addConstraint(new ContinuousCollisionConstraint(
                simulator.getStaticBodies(),
                config.getGravity()
        ));

        // Spawn balls randomly distributed
        Random random = new Random(42);  // Fixed seed for reproducibility
        for (int i = 0; i < ballCount; i++) {
            double x = 5 + random.nextDouble() * (worldWidth - 10);
            double y = groundHeight + 5 + random.nextDouble() * (worldHeight - groundHeight - 10);
            double vx = (random.nextDouble() - 0.5) * 10;
            double vy = (random.nextDouble() - 0.5) * 10;

            Ball ball = new Ball(
                    new Vector(x, y),
                    new Vector(vx, vy),
                    Collections.singletonList(config.getGravity()),
                    config.getDefaultMaterial()
            );
            simulator.addBody(ball);
        }

        return simulator;
    }

    @Test
    void testSpatialPartitioningCorrectness() {
        System.out.println("\n=== Correctness Test: Spatial Partitioning ===");

        int ballCount = 200;
        int frameCount = 50;
        double worldWidth = 120.0;
        double worldHeight = 80.0;

        // Create two identical simulations with same random seed
        PhysicsConfig configWithout = new PhysicsConfig();
        configWithout.setUseSpatialPartitioning(false);
        configWithout.setSubsteps(1);

        PhysicsConfig configWith = new PhysicsConfig();
        configWith.setUseSpatialPartitioning(true);
        configWith.setGridCellSize(0.5);
        configWith.setSubsteps(1);

        PhysicsSimulator simWithout = createSimulator(configWithout, ballCount, worldWidth, worldHeight);
        PhysicsSimulator simWith = createSimulator(configWith, ballCount, worldWidth, worldHeight);
        simWith.setWorldBounds(0, worldWidth, 0, worldHeight);

        // Run both simulations and verify they produce same results
        for (int frame = 0; frame < frameCount; frame++) {
            simWithout.update(1.0 / 60.0);
            simWith.update(1.0 / 60.0);

            // Check positions match (within floating point tolerance)
            var bodiesWithout = simWithout.getDynamicBodies();
            var bodiesWith = simWith.getDynamicBodies();

            for (int i = 0; i < bodiesWithout.size(); i++) {
                Vector posWithout = bodiesWithout.get(i).getPosition();
                Vector posWith = bodiesWith.get(i).getPosition();

                double dx = Math.abs(posWithout.x() - posWith.x());
                double dy = Math.abs(posWithout.y() - posWith.y());

                // Allow small tolerance due to HashSet iteration order differences
                assertTrue(dx < 0.1 && dy < 0.1,
                        String.format("Frame %d, Ball %d: Positions differ too much (%.4f, %.4f)",
                                frame, i, dx, dy));
            }
        }

        System.out.println("✓ Spatial partitioning produces same results as naive method");
    }
}
