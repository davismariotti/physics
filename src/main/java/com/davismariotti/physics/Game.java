package com.davismariotti.physics;

import com.davismariotti.physics.constraints.BoundaryConstraint;
import com.davismariotti.physics.core.PhysicsConfig;
import com.davismariotti.physics.core.PhysicsSimulator;
import com.davismariotti.physics.input.InputHandler;
import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.rendering.Renderer;
import com.davismariotti.physics.sprites.Ray;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.*;

/**
 * Main orchestrator for the physics simulation
 */
public class Game extends JFrame {

    // Legacy static fields for backward compatibility
    public static Vector GRAVITY;
    public static double SCALE;
    public static double GAME_SPEED;

    private final PhysicsSimulator simulator;
    private final InputHandler inputHandler;
    private final Renderer renderer;
    private final Ray ray;

    private boolean isRunning = true;
    private final int fps = 30;
    private final int windowWidth = 1200;
    private final int windowHeight = 800;

    public static void main(String[] args) {
        Game game = new Game();
        game.run();
        System.exit(0);
    }

    public Game() {
        // Initialize physics configuration
        PhysicsConfig config = new PhysicsConfig();

        // Set static fields for backward compatibility
        GRAVITY = config.getGravity();
        SCALE = config.getScale();
        GAME_SPEED = config.getGameSpeed();

        // Create ray (aim indicator)
        ray = new Ray(Vector.ZERO, 30, 45, 50);

        // Create physics simulator
        simulator = new PhysicsSimulator(config);

        // Add boundary constraint
        BoundaryConstraint boundaryConstraint = new BoundaryConstraint(
                0, windowWidth / SCALE,
                0, windowHeight / SCALE
        );
        simulator.addConstraint(boundaryConstraint);

        // Create input handler
        inputHandler = new InputHandler(simulator, ray);

        // Create renderer
        renderer = new Renderer(windowWidth, windowHeight, simulator, ray);

        // Set up keyboard listener
        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                inputHandler.keyPressed(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                inputHandler.keyReleased(e.getKeyCode());
            }
        });
    }

    /**
     * Main game loop
     */
    public void run() {
        initialize();

        while (isRunning) {
            long frameStart = System.currentTimeMillis();

            // Process input
            inputHandler.processInput();

            // Update physics
            simulator.update(1.0 / fps * GAME_SPEED);

            // Render
            renderer.render(getGraphics(), getInsets());

            // Frame timing
            long frameTime = System.currentTimeMillis() - frameStart;
            long sleepTime = (1000 / fps) - frameTime;

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (Exception ignored) {
                }
            }

            // Calculate and update FPS
            long totalFrameTime = System.currentTimeMillis() - frameStart;
            double actualFps = totalFrameTime > 0 ? 1000.0 / totalFrameTime : fps;
            renderer.setActualFps(actualFps);
        }

        setVisible(false);
    }

    /**
     * Initialize the game window
     */
    void initialize() {
        setTitle("Physics Simulation");
        setSize(windowWidth, windowHeight);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);

        Insets insets = getInsets();
        setSize(insets.left + windowWidth + insets.right,
                insets.top + windowHeight + insets.bottom);
    }
}
