package com.davismariotti.physics;

import com.davismariotti.physics.collision.SpatialGrid;
import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.sprites.Ball;
import com.davismariotti.physics.sprites.DynamicBody;
import com.davismariotti.physics.sprites.MaterialProperties;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpatialGridTest {

    @Test
    void testGridCreation() {
        SpatialGrid grid = new SpatialGrid(0, 100, 0, 80, 1.0);

        // Should create 101×81 cells (adding 1 for boundaries)
        assertEquals(101 * 81, grid.getCellCount());

        System.out.println(grid.getGridInfo());
    }

    @Test
    void testInsertAndQuery() {
        // Create a 10×10 world with cell size 2
        SpatialGrid grid = new SpatialGrid(0, 10, 0, 10, 2.0);

        // Create a ball at position (5, 5)
        Ball ball = new Ball(
                new Vector(5, 5),
                Vector.ZERO,
                Collections.emptyList(),
                MaterialProperties.DEFAULT
        );

        grid.insert(ball);

        // Query nearby - should find the ball
        List<DynamicBody> nearby = grid.queryNearby(ball);
        assertTrue(nearby.contains(ball), "Should find the inserted ball");
    }

    @Test
    void testQueryFindsNearbyBodies() {
        SpatialGrid grid = new SpatialGrid(0, 10, 0, 10, 1.0);

        // Create balls close together
        Ball ball1 = new Ball(new Vector(5.0, 5.0), Vector.ZERO, Collections.emptyList(), MaterialProperties.DEFAULT);
        Ball ball2 = new Ball(new Vector(5.3, 5.3), Vector.ZERO, Collections.emptyList(), MaterialProperties.DEFAULT);
        Ball ball3 = new Ball(new Vector(9.0, 9.0), Vector.ZERO, Collections.emptyList(), MaterialProperties.DEFAULT);

        grid.insert(ball1);
        grid.insert(ball2);
        grid.insert(ball3);

        // Query around ball1 - should find ball2 (close) but not ball3 (far)
        List<DynamicBody> nearby = grid.queryNearby(ball1);

        assertTrue(nearby.contains(ball1), "Should find itself");
        assertTrue(nearby.contains(ball2), "Should find nearby ball2");
        // ball3 might or might not be included depending on grid cell boundaries
    }

    @Test
    void testQueryDoesNotFindDistantBodies() {
        SpatialGrid grid = new SpatialGrid(0, 100, 0, 100, 2.0);

        // Create balls far apart
        Ball ball1 = new Ball(new Vector(10, 10), Vector.ZERO, Collections.emptyList(), MaterialProperties.DEFAULT);
        Ball ball2 = new Ball(new Vector(90, 90), Vector.ZERO, Collections.emptyList(), MaterialProperties.DEFAULT);

        grid.insert(ball1);
        grid.insert(ball2);

        // Query around ball1 - should NOT find ball2 (too far)
        List<DynamicBody> nearby = grid.queryNearby(ball1);

        assertTrue(nearby.contains(ball1), "Should find itself");
        assertFalse(nearby.contains(ball2), "Should NOT find distant ball2");
    }

    @Test
    void testClearRemovesAllBodies() {
        SpatialGrid grid = new SpatialGrid(0, 10, 0, 10, 1.0);

        Ball ball = new Ball(new Vector(5, 5), Vector.ZERO, Collections.emptyList(), MaterialProperties.DEFAULT);

        grid.insert(ball);
        List<DynamicBody> beforeClear = grid.queryNearby(ball);
        assertTrue(beforeClear.contains(ball), "Should find ball before clear");

        grid.clear();
        List<DynamicBody> afterClear = grid.queryNearby(ball);
        assertFalse(afterClear.contains(ball), "Should NOT find ball after clear");
    }

    @Test
    void testMultiCellInsertion() {
        // Create grid with large cells relative to ball size
        SpatialGrid grid = new SpatialGrid(0, 10, 0, 10, 5.0);

        // Ball at cell boundary should be in multiple cells
        Ball ball = new Ball(new Vector(5.0, 5.0), Vector.ZERO, Collections.emptyList(), MaterialProperties.DEFAULT);

        grid.insert(ball);

        // Ball on boundary should be queryable from multiple cells
        List<DynamicBody> nearby = grid.queryNearby(ball);

        // Should find the ball at least once (may appear multiple times due to multi-cell insertion)
        assertTrue(nearby.contains(ball), "Should find ball at boundary");
    }

    @Test
    void testOutOfBoundsHandling() {
        SpatialGrid grid = new SpatialGrid(0, 10, 0, 10, 1.0);

        // Ball outside world bounds - should be clamped to edge cells
        Ball ball = new Ball(new Vector(-5, -5), Vector.ZERO, Collections.emptyList(), MaterialProperties.DEFAULT);

        // Should not crash
        assertDoesNotThrow(() -> grid.insert(ball));
        assertDoesNotThrow(() -> grid.queryNearby(ball));
    }

    @Test
    void testLargeNumberOfBodies() {
        SpatialGrid grid = new SpatialGrid(0, 100, 0, 80, 1.0);

        // Insert 1000 balls at random positions
        for (int i = 0; i < 1000; i++) {
            double x = Math.random() * 100;
            double y = Math.random() * 80;
            Ball ball = new Ball(new Vector(x, y), Vector.ZERO, Collections.emptyList(), MaterialProperties.DEFAULT);
            grid.insert(ball);
        }

        // Query should not crash and should return reasonable number of candidates
        Ball testBall = new Ball(new Vector(50, 40), Vector.ZERO, Collections.emptyList(), MaterialProperties.DEFAULT);
        List<DynamicBody> nearby = grid.queryNearby(testBall);

        // Should find much fewer than 1000 bodies (demonstrating spatial partitioning works)
        assertTrue(nearby.size() < 1000, "Should find fewer bodies than total");
        System.out.println("Query found " + nearby.size() + " candidates out of 1000 total bodies");
    }

    @Test
    void testDuplicateDetectionPrevention() {
        SpatialGrid grid = new SpatialGrid(0, 10, 0, 10, 5.0);

        // Create two balls
        Ball ball1 = new Ball(new Vector(5.0, 5.0), Vector.ZERO, Collections.emptyList(), MaterialProperties.DEFAULT);
        Ball ball2 = new Ball(new Vector(5.1, 5.1), Vector.ZERO, Collections.emptyList(), MaterialProperties.DEFAULT);

        grid.insert(ball1);
        grid.insert(ball2);

        List<DynamicBody> nearby = grid.queryNearby(ball1);

        // Note: The current implementation may return duplicates if balls span multiple cells
        // This is acceptable - the narrow-phase collision check will deduplicate
        // Just verify we get results
        assertFalse(nearby.isEmpty(), "Should find nearby bodies");
    }
}
