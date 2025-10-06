package com.davismariotti.physics.input;

import com.davismariotti.physics.core.PhysicsSimulator;
import com.davismariotti.physics.sprites.Ball;
import com.davismariotti.physics.sprites.Ray;

import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles keyboard input and updates game state accordingly
 */
public class InputHandler {
    private final Set<Integer> pressed = new HashSet<>();
    private final PhysicsSimulator simulator;
    private final Ray ray;

    public InputHandler(PhysicsSimulator simulator, Ray ray) {
        this.simulator = simulator;
        this.ray = ray;
    }

    public synchronized void keyPressed(int keyCode) {
        pressed.add(keyCode);
    }

    public synchronized void keyReleased(int keyCode) {
        pressed.remove(keyCode);
    }

    /**
     * Process currently pressed keys and update game state
     */
    public void processInput() {
        if (pressed.size() >= 1) {
            for (int pressedCode : pressed) {
                if (pressedCode == KeyEvent.VK_LEFT) {
                    ray.addAngle(3);
                }
                if (pressedCode == KeyEvent.VK_RIGHT) {
                    ray.addAngle(-3);
                }
                if (pressedCode == KeyEvent.VK_ENTER) {
                    Ball ball = new Ball(
                            ray.getPosition(),
                            ray.getUnitVector().multiply(40),
                            Collections.singletonList(simulator.getConfig().getGravity()),
                            simulator.getConfig().getCoefficientOfRestitution(),
                            simulator.getConfig().getDragCoefficient()
                    );
                    simulator.addBody(ball);
                }
                if (pressedCode == KeyEvent.VK_UP) {
                    double newDrag = Math.min(1.0, simulator.getConfig().getDragCoefficient() + 0.001);
                    simulator.updateDragCoefficient(newDrag);
                }
                if (pressedCode == KeyEvent.VK_DOWN) {
                    double newDrag = Math.max(0.0, simulator.getConfig().getDragCoefficient() - 0.001);
                    simulator.updateDragCoefficient(newDrag);
                }
                if (pressedCode == KeyEvent.VK_R) {
                    double newRestitution = Math.min(1.0, simulator.getConfig().getCoefficientOfRestitution() + 0.05);
                    simulator.updateCoefficientOfRestitution(newRestitution);
                }
                if (pressedCode == KeyEvent.VK_F) {
                    double newRestitution = Math.max(0.0, simulator.getConfig().getCoefficientOfRestitution() - 0.05);
                    simulator.updateCoefficientOfRestitution(newRestitution);
                }
                if (pressedCode == KeyEvent.VK_Q) {
                    System.exit(0);
                }
            }
        }
    }
}
