package com.davismariotti.physics.collision;

import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.sprites.DynamicBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Uniform spatial grid for broad-phase collision detection
 * Divides world space into cells to quickly find potential collision pairs
 *
 * Performance: O(1) insertion, O(k) query where k is objects in nearby cells
 * vs O(n²) naive collision detection
 *
 * Uses HashMap for sparse storage - only allocates cells that contain objects
 */
public class SpatialGrid {
    private final double cellSize;
    private final int gridWidth;
    private final int gridHeight;
    private final double worldMinX;
    private final double worldMinY;
    private final double worldMaxX;
    private final double worldMaxY;

    // Grid cells stored as HashMap: only non-empty cells are allocated
    // Key: cellIndex = y * gridWidth + x
    private final Map<Integer, List<DynamicBody>> cells;

    /**
     * Create a spatial grid covering the specified world bounds
     *
     * @param worldMinX minimum X coordinate of world
     * @param worldMaxX maximum X coordinate of world
     * @param worldMinY minimum Y coordinate of world
     * @param worldMaxY maximum Y coordinate of world
     * @param cellSize size of each grid cell (should be ~2x object radius)
     */
    public SpatialGrid(double worldMinX, double worldMaxX, double worldMinY, double worldMaxY, double cellSize) {
        this.cellSize = cellSize;
        this.worldMinX = worldMinX;
        this.worldMinY = worldMinY;
        this.worldMaxX = worldMaxX;
        this.worldMaxY = worldMaxY;

        // Calculate grid dimensions (add 1 to ensure we cover boundaries)
        this.gridWidth = (int) Math.ceil((worldMaxX - worldMinX) / cellSize) + 1;
        this.gridHeight = (int) Math.ceil((worldMaxY - worldMinY) / cellSize) + 1;

        // Use HashMap for sparse storage - only allocate non-empty cells
        this.cells = new HashMap<>();
    }

    /**
     * Clear all cells (call at start of each frame)
     * O(occupied cells) operation - only clears non-empty cells
     */
    public void clear() {
        cells.clear();
    }

    /**
     * Insert a dynamic body into the grid
     * Bodies near cell boundaries are inserted into multiple cells to avoid missed collisions
     *
     * @param body the body to insert
     */
    public void insert(DynamicBody body) {
        CircleCollider collider = (CircleCollider) body.getCollider();
        Vector center = collider.center();
        double radius = collider.radius();

        // Calculate bounding box of the circle
        double minX = center.x() - radius;
        double maxX = center.x() + radius;
        double minY = center.y() - radius;
        double maxY = center.y() + radius;

        // Convert world coordinates to grid coordinates
        int minCellX = worldToGridX(minX);
        int maxCellX = worldToGridX(maxX);
        int minCellY = worldToGridY(minY);
        int maxCellY = worldToGridY(maxY);

        // Insert into all cells that the bounding box overlaps
        for (int gy = minCellY; gy <= maxCellY; gy++) {
            for (int gx = minCellX; gx <= maxCellX; gx++) {
                if (isValidCell(gx, gy)) {
                    int cellIndex = gy * gridWidth + gx;
                    // Get or create cell list
                    List<DynamicBody> cell = cells.computeIfAbsent(cellIndex, k -> new ArrayList<>());
                    cell.add(body);
                }
            }
        }
    }

    /**
     * Query all bodies in cells near the given body
     * Returns potential collision candidates (requires narrow-phase check)
     *
     * @param body the body to query around
     * @return list of potential collision candidates
     */
    public List<DynamicBody> queryNearby(DynamicBody body) {
        List<DynamicBody> nearby = new ArrayList<>();

        CircleCollider collider = (CircleCollider) body.getCollider();
        Vector center = collider.center();
        double radius = collider.radius();

        // Calculate bounding box (same as insert)
        double minX = center.x() - radius;
        double maxX = center.x() + radius;
        double minY = center.y() - radius;
        double maxY = center.y() + radius;

        int minCellX = worldToGridX(minX);
        int maxCellX = worldToGridX(maxX);
        int minCellY = worldToGridY(minY);
        int maxCellY = worldToGridY(maxY);

        // Collect all bodies from overlapping cells
        for (int gy = minCellY; gy <= maxCellY; gy++) {
            for (int gx = minCellX; gx <= maxCellX; gx++) {
                if (isValidCell(gx, gy)) {
                    int cellIndex = gy * gridWidth + gx;
                    List<DynamicBody> cell = cells.get(cellIndex);
                    if (cell != null) {
                        nearby.addAll(cell);
                    }
                }
            }
        }

        return nearby;
    }

    /**
     * Convert world X coordinate to grid X index
     */
    private int worldToGridX(double worldX) {
        int gx = (int) ((worldX - worldMinX) / cellSize);
        // Clamp to valid range
        return Math.max(0, Math.min(gridWidth - 1, gx));
    }

    /**
     * Convert world Y coordinate to grid Y index
     */
    private int worldToGridY(double worldY) {
        int gy = (int) ((worldY - worldMinY) / cellSize);
        // Clamp to valid range
        return Math.max(0, Math.min(gridHeight - 1, gy));
    }

    /**
     * Check if grid coordinates are within bounds
     */
    private boolean isValidCell(int gx, int gy) {
        return gx >= 0 && gx < gridWidth && gy >= 0 && gy < gridHeight;
    }

    /**
     * Get total number of possible cells in the grid
     */
    public int getTotalCellCount() {
        return gridWidth * gridHeight;
    }

    /**
     * Get number of occupied (non-empty) cells
     */
    public int getOccupiedCellCount() {
        return cells.size();
    }

    /**
     * Get grid dimensions for debugging
     */
    public String getGridInfo() {
        return String.format("SpatialGrid[%d×%d cells, cellSize=%.2f, world=(%.1f,%.1f)-(%.1f,%.1f)]",
                gridWidth, gridHeight, cellSize, worldMinX, worldMinY, worldMaxX, worldMaxY);
    }

    /**
     * Get statistics about cell occupancy for performance analysis
     */
    public String getOccupancyStats() {
        int maxOccupancy = 0;
        int totalBodies = 0;

        for (List<DynamicBody> cell : cells.values()) {
            int size = cell.size();
            maxOccupancy = Math.max(maxOccupancy, size);
            totalBodies += size;
        }

        int occupiedCells = cells.size();
        double avgOccupancy = occupiedCells > 0 ? (double) totalBodies / occupiedCells : 0;

        return String.format("Grid occupancy: %d/%d cells used, avg=%.1f bodies/cell, max=%d bodies/cell",
                occupiedCells, getTotalCellCount(), avgOccupancy, maxOccupancy);
    }
}
