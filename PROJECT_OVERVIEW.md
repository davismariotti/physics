# Physics Simulation Engine - Project Overview

## Quick Summary
A 2D rigid body physics simulation engine in Java featuring continuous collision detection, spatial partitioning for O(n) collision detection, and a sleeping system for performance optimization. The engine simulates circles and AABBs with realistic friction, restitution, and drag. Built with Java 21 and Java Swing for visualization.

## Core Algorithms

### 1. Time of Impact (TOI) Collision Detection
**Location**: `src/main/java/com/davismariotti/physics/collision/TOISolver.java`

Analytical continuous collision detection that calculates the exact moment when two bodies first make contact during a timestep. Uses quadratic equation solving for:
- **Circle-AABB collisions**: Solves `y(t) = y0 + v0*t + 0.5*a*t^2 = groundTop + radius`
- **Circle-Circle collisions**: Uses relative frame of reference, solves `|relPos + relVel*t| = combinedRadius`

Advantages: Prevents tunneling, enables accurate collision response at exact moment of impact. Fallback to discrete correction if TOI fails.

### 2. Spatial Partitioning (Uniform Grid)
**Location**: `src/main/java/com/davismariotti/physics/collision/SpatialGrid.java`

HashMap-based uniform grid that divides world space into cells for broad-phase collision detection.
- **Insertion**: O(1) per object, bodies inserted into all cells their AABB overlaps
- **Query**: O(k) where k = objects in nearby cells (typically 10-50 vs 1000+ total)
- **Storage**: Sparse storage - only allocates non-empty cells
- **Cell size**: Configurable, typically ~2x object radius for optimal performance

Reduces collision detection from O(n²) to O(n) for large numbers of objects.

### 3. Sequential Impulse Solver
**Location**: `src/main/java/com/davismariotti/physics/constraints/DynamicCollisionConstraint.java:84-93`

Iterative constraint solver that refines velocity solutions over multiple iterations:
- Multiple passes over all collisions per frame
- Each iteration applies impulse to resolve penetration/separation
- Improves convergence in complex scenarios (stacks, chains)
- Currently configured for 1 iteration (can be increased via `PhysicsConfig.velocityIterations`)

Reduces jitter and improves stability in piles of objects.

### 4. Sleeping System
**Location**: `src/main/java/com/davismariotti/physics/sprites/DynamicBody.java:222-240`

Performance optimization that deactivates settled bodies:
- Bodies sleep when velocity < threshold for N consecutive frames (default: 0.5 units/s for 30 frames)
- Sleeping bodies skip physics update entirely
- Woken when collision with non-sleeping body occurs (with wake threshold to prevent jitter)
- Velocity zeroed when sleeping to prevent drift

Enables handling 1000+ bodies by skipping inactive settled stacks.

### 5. Verlet Integration
**Location**: `src/main/java/com/davismariotti/physics/sprites/DynamicBody.java:53-73`

Semi-implicit Euler integration with substeps:
- Position: `p' = p + v*dt + 0.5*a*dt²`
- Velocity: `v' = v + a*dt`
- Substeps (default: 6) improve accuracy by using smaller timesteps
- Velocity clamping at 200 units/s prevents runaway speeds

### 6. Two-Body Momentum Exchange
**Location**: `src/main/java/com/davismariotti/physics/constraints/DynamicCollisionConstraint.java:245-285`

Conservation of momentum in collisions:
- Normal impulse: `j = -(1 + e) * (vB - vA)·n / (1/mA + 1/mB)`
- Applied equally and oppositely to both bodies
- Restitution coefficient (e) controls bounciness (0 = inelastic, 1 = elastic)
- Works for bodies of different masses

### 7. Coulomb Friction
**Location**: `src/main/java/com/davismariotti/physics/constraints/DynamicCollisionConstraint.java:291-348`

Realistic friction model:
- Tangent impulse proportional to normal force: `|jt| ≤ μ * |jn|`
- Uses pre-impulse relative velocity for correct tangent calculation
- Dynamic friction coefficient combined using Pythagorean theorem
- Only applied for low-restitution contacts (restitution < 0.3) to avoid friction on bouncy collisions

## Architecture

### Package Structure
```
com.davismariotti.physics/
├── collision/          # Collision detection and spatial partitioning
│   ├── TOISolver.java           # Time of impact calculations
│   ├── SpatialGrid.java         # O(n) broad-phase collision
│   ├── CollisionDetector.java   # Narrow-phase collision
│   ├── CircleCollider.java      # Circle collision shapes
│   └── AABBCollider.java        # Box collision shapes
├── constraints/        # Constraint solvers
│   ├── DynamicCollisionConstraint.java  # Ball-ball collisions
│   ├── ContinuousCollisionConstraint.java  # Ball-static collisions
│   └── BoundaryConstraint.java          # World boundaries
├── core/              # Physics engine core
│   ├── PhysicsSimulator.java    # Main simulation loop
│   └── PhysicsConfig.java       # Configuration
├── forces/            # Force generators
│   ├── GravityForce.java
│   └── DragForce.java
├── sprites/           # Rigid bodies
│   ├── DynamicBody.java  # Moving bodies (abstract)
│   ├── StaticBody.java   # Immovable bodies (abstract)
│   ├── Ball.java         # Circle rigid body
│   ├── Ground.java       # Static AABB platform
│   └── MaterialProperties.java  # Restitution, friction, drag
├── kinematics/        # Vector mathematics
│   └── Vector.java
├── rendering/         # Visualization
│   ├── Renderer.java
│   ├── Camera.java
│   └── WorldRenderer.java
├── interactions/      # User interactions
│   ├── WorldInteractionSystem.java
│   └── AimIndicator.java  # Ball launcher
└── input/            # Input handling
    └── InputHandler.java
```

## Key Classes

### PhysicsSimulator (`core/PhysicsSimulator.java`)
Main orchestrator of the physics simulation:
- Manages lists of dynamic and static bodies
- Applies global forces (gravity, drag)
- Runs substeps for accuracy
- Applies constraints (collisions, boundaries)
- Updates sleep states

**Update Loop** (100 FPS, 6 substeps):
```
for each substep:
  for each awake body:
    store previous state (for TOI)
    apply forces
    integrate position/velocity
    apply constraints (static collisions, boundaries)
  apply dynamic collision constraint (ball-ball, with velocity iterations)
update sleep states
```

### DynamicBody (`sprites/DynamicBody.java`)
Abstract base for moving rigid bodies:
- Stores current and previous position/velocity (for TOI)
- Tracks forces (persistent + temporary)
- Manages sleep state and resting frame counter
- Provides integration step

### DynamicCollisionConstraint (`constraints/DynamicCollisionConstraint.java`)
Handles all ball-to-ball collisions:
- Broad phase: Spatial grid query or naive O(n²)
- Narrow phase: TOI calculation
- Response: Rewind to TOI, apply impulse with friction, integrate forward
- Supports velocity iterations for better convergence

### SpatialGrid (`collision/SpatialGrid.java`)
Uniform grid for fast spatial queries:
- HashMap storage for sparse occupancy
- Cell size = 0.5 world units (configurable)
- Query returns candidates, still needs narrow-phase check
- Provides occupancy statistics for debugging

## Physics Features

### Supported Features
- **Gravity**: Configurable constant downward force (default: -9.8 units/s²)
- **Drag**: Quadratic air resistance proportional to velocity² (configurable coefficient)
- **Restitution**: Bounciness coefficient 0-1 (0 = inelastic, 1 = perfectly elastic)
- **Friction**: Static and dynamic friction using Coulomb model
- **Continuous Collision**: TOI-based collision detection prevents tunneling
- **Substeps**: Multiple integration steps per frame for accuracy (default: 6)
- **Material Properties**: Per-body properties (restitution, drag, friction)

### Current Limitations
- Only circles (Ball) and axis-aligned boxes (Ground) supported
- No rotation (pure translation)
- No joints or constraints between bodies
- Ground-ball collision uses simplified approach (horizontal top face only)

## Performance Optimizations

### 1. Spatial Partitioning
- Enabled by default in `PhysicsConfig.useSpatialPartitioning`
- Reduces collision checks from O(n²) to O(n)
- Critical for >100 bodies
- Cell size: 0.5 units (adjustable via `PhysicsConfig.gridCellSize`)

### 2. Sleeping System
- Enabled by default in `PhysicsConfig.useSleeping`
- Sleep threshold: 0.5 units/s for 30 frames
- Sleeping bodies skip physics entirely
- Wake threshold prevents jitter from waking settled piles
- Can handle 2000+ bodies with most sleeping

### 3. Substeps
- Default: 6 substeps per frame
- Smaller timesteps improve accuracy and stability
- Trade-off: More substeps = better accuracy but slower

### 4. Velocity Iterations
- Default: 1 (effectively disabled)
- Can be increased to 4-8 for better stability in complex scenarios
- Each iteration refines the velocity solution

### Benchmark Results (from `SpatialPartitioningBenchmark.java`)
- **100 balls**:
  - Naive O(n²): ~4,950 collision checks
  - Spatial grid: ~600 collision checks (8.25x faster)
- **500 balls**:
  - Naive O(n²): ~124,750 checks
  - Spatial grid: ~3,500 checks (35.6x faster)

## Configuration

### PhysicsConfig Parameters (`core/PhysicsConfig.java`)

| Parameter | Default | Description |
|-----------|---------|-------------|
| `gravity` | (0, -9.8) | Gravity vector (world units/s²) |
| `scale` | 10.0 | Screen pixels per world unit |
| `gameSpeed` | 3.0 | Time multiplier (3x = 3s of physics per real second) |
| `substeps` | 6 | Integration substeps per frame |
| `useSpatialPartitioning` | true | Enable spatial grid broad-phase |
| `gridCellSize` | 0.5 | Spatial grid cell size (world units) |
| `useSleeping` | true | Enable sleeping for settled bodies |
| `sleepVelocityThreshold` | 0.5 | Velocity below which bodies can sleep |
| `sleepFramesRequired` | 30 | Consecutive low-velocity frames to sleep |
| `velocityIterations` | 1 | Sequential impulse iterations |
| `defaultMaterial` | MaterialProperties.DEFAULT | Default material properties |

### MaterialProperties
- `coefficientOfRestitution`: 0.3 (default) - bounciness
- `dragCoefficient`: 0.001 (default) - air resistance
- `staticFriction`: 0.3 (default) - friction when stationary
- `dynamicFriction`: 0.2 (default) - friction when sliding

## Interaction System

### WorldInteractionSystem (`interactions/WorldInteractionSystem.java`)
Manages interactive elements that respond to user input and render to screen:
- List of `WorldInteraction` implementations
- Updates each interaction per frame
- Routes input to interactions
- Renders interactions after physics objects

### AimIndicator (`interactions/AimIndicator.java`)
Interactive ball launcher:
- Arrow keys rotate aim direction
- Enter key spawns ball with initial velocity
- Debounced input (500ms delay, max 5 rapid-fire)
- Spawns balls at end of launcher ray

### InputHandler (`input/InputHandler.java`)
Keyboard input processing:
- Tracks pressed keys
- Routes to world interaction system
- Handles continuous input (held keys)

## Test Suite

Located in `src/test/java/com/davismariotti/physics/`

### Physics Tests
- **BallCollisionTest**: Verifies TOI collision detection and response
- **EnergyConservationTest**: Validates energy conservation in elastic collisions
- **HorizontalMomentumTest**: Checks momentum conservation
- **SimpleCollisionTest**: Basic collision scenarios
- **FrictionTest**: Friction behavior validation

### Performance Tests
- **SpatialPartitioningBenchmark**: Compares naive vs spatial grid performance
- **SpatialGridTest**: Validates spatial grid correctness

## Recent Improvements (from git history)

1. **Velocity Iterations** (commit e85ffcd): Added sequential impulse iterations for better convergence
2. **Sleeping System** (commit 1e568db): Performance optimization for settled bodies
3. **Spatial Partitioning** (commit c98a35a): O(n²) → O(n) collision detection with uniform grid
4. **Smaller Grid Cells** (commit 899ee25): Optimized cell size from 20 to 0.5 units

## Entry Point

**Main class**: `com.davismariotti.physics.Game` (`src/main/java/com/davismariotti/physics/Game.java`)
- Creates `PhysicsSimulator` with `PhysicsConfig`
- Sets up ground, boundaries, and collision constraints
- Initializes `AimIndicator` interaction
- Runs game loop at 100 FPS: `input → physics update → render`

## Future Ideas (from IDEAS.md)

Potential enhancements ranked by complexity:
- **Easy**: Explosion system, slow motion, color by velocity, mouse interaction
- **Medium**: Static obstacles placement, different ball types, wind fields, rope constraints
- **Advanced**: Ragdoll/soft body, destructible objects, fluid simulation (SPH), save/load scenes

## Quick Start for AI

When working on this codebase:
1. Physics loop is in `PhysicsSimulator.update()` (core/PhysicsSimulator.java:93)
2. Collision detection starts in `DynamicCollisionConstraint.applyAll()` (constraints/DynamicCollisionConstraint.java:84)
3. TOI calculation in `TOISolver.computeTOI()` (collision/TOISolver.java)
4. To add new body type: extend `DynamicBody` or `StaticBody`, implement `getCollider()`
5. To add new force: implement `Force` interface, add to `PhysicsSimulator.globalForces`
6. Configuration changes go through `PhysicsConfig` class
