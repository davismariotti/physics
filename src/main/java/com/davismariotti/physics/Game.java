package com.davismariotti.physics;

import com.davismariotti.physics.kinematics.Axis;
import com.davismariotti.physics.kinematics.TensionForce;
import com.davismariotti.physics.kinematics.Vector;
import com.davismariotti.physics.sprites.Ball;
import com.davismariotti.physics.sprites.Ray;

import java.awt.geom.AffineTransform;
import java.util.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.*;

/**
 * Main class for the game
 */
public class Game extends JFrame {

    public static Vector GRAVITY = new Vector(0, -9.8);
    public static double SCALE = 10.0;
    public static double GAME_SPEED = 3.0;

    double coefficientOfRestitution = 0.9;
    double dragCoefficient = 0.0;
    double actualFps = 0.0;

    boolean isRunning = true;
    int fps = 30;
    int windowWidth = 1200;
    int windowHeight = 800;

    BufferedImage backBuffer;
    Insets insets;

    List<Ball> balls = new ArrayList<>();
    Ray ray = new Ray(Vector.ZERO, 30, 45, 50);

    // Set of currently pressed keys
    private final Set<Integer> pressed = new HashSet<>();

    public static void main(String[] args) {
//        Ball ball = new Ball(new Vector(50, 50), Vector.ZERO, Collections.singletonList(GRAVITY));
//        Vector origin = new Vector(40, 20);
//        TensionForce force = new TensionForce(origin, ball);
//        ball.setForces(Collections.singletonList(force));
//        double degrees = Math.atan2(force.getVectorBetweenPoints().getY(), force.getVectorBetweenPoints().getX()) / Math.PI * 180 + 90;
//        System.out.println(degrees);

        Game game = new Game();
        game.run();
        System.exit(0);
    }

    public Game() {
        addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public synchronized void keyPressed(KeyEvent e) {
                pressed.add(e.getKeyCode());
            }

            @Override
            public synchronized void keyReleased(KeyEvent e) {
                pressed.remove(e.getKeyCode());
            }
        });
    }

    /**
     * This method starts the game and runs it in a loop
     */
    public void run() {
        initialize();

        while (isRunning) {
            long frameStart = System.currentTimeMillis();

            if (pressed.size() >= 1) {
                for (int pressedCode : pressed) {
                    if (pressedCode == KeyEvent.VK_LEFT) {
                        ray.addAngle(3);
                    }
                    if (pressedCode == KeyEvent.VK_RIGHT) {
                        ray.addAngle(-3);
                    }
                    if (pressedCode == KeyEvent.VK_ENTER) {
                        Ball ball = new Ball(ray.getPosition(), ray.getUnitVector().multiply(40), Collections.singletonList(GRAVITY), coefficientOfRestitution, dragCoefficient);
                        balls.listIterator().add(ball);
                    }
                    if (pressedCode == KeyEvent.VK_UP) {
                        dragCoefficient = Math.min(1.0, dragCoefficient + 0.001);
                        updateAllBallCoefficients();
                    }
                    if (pressedCode == KeyEvent.VK_DOWN) {
                        dragCoefficient = Math.max(0.0, dragCoefficient - 0.001);
                        updateAllBallCoefficients();
                    }
                    if (pressedCode == KeyEvent.VK_R) {
                        coefficientOfRestitution = Math.min(1.0, coefficientOfRestitution + 0.05);
                        updateAllBallCoefficients();
                    }
                    if (pressedCode == KeyEvent.VK_F) {
                        coefficientOfRestitution = Math.max(0.0, coefficientOfRestitution - 0.05);
                        updateAllBallCoefficients();
                    }
                    if (pressedCode == KeyEvent.VK_Q) {
                        System.exit(0);
                    }
                }
            }

            update();
            draw();

            //  delay for each frame  -   time it took for one frame
            long frameTime = System.currentTimeMillis() - frameStart;
            long sleepTime = (1000 / fps) - frameTime;

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (Exception ignored) {
                }
            }

            // Calculate actual FPS based on total frame time (including sleep)
            long totalFrameTime = System.currentTimeMillis() - frameStart;
            actualFps = totalFrameTime > 0 ? 1000.0 / totalFrameTime : fps;
        }

        setVisible(false);
    }

    /**
     * This method will set up everything need for the game to run
     */
    void initialize() {
        setTitle("Game Tutorial");
        setSize(windowWidth, windowHeight);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);

        insets = getInsets();
        setSize(insets.left + windowWidth + insets.right,
                insets.top + windowHeight + insets.bottom);

        backBuffer = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);
    }

    void update() {
        for (ListIterator<Ball> it = balls.listIterator(); it.hasNext(); ) {
            Ball ball = it.next();
            ball.update(1.0 / fps * GAME_SPEED);

            // Check X boundaries
            if (ball.getPosition().getX() > windowWidth / SCALE) {
                ball.setPosition(new Vector(windowWidth / SCALE, ball.getPosition().getY()));
                ball.flipAboutAxis(Axis.Y);
            } else if (ball.getPosition().getX() < 0) {
                ball.setPosition(new Vector(0, ball.getPosition().getY()));
                ball.flipAboutAxis(Axis.Y);
            }

            // Check Y boundaries
            if (ball.getPosition().getY() > windowHeight / SCALE) {
                ball.setPosition(new Vector(ball.getPosition().getX(), windowHeight / SCALE));
                ball.flipAboutAxis(Axis.X);
            } else if (ball.getPosition().getY() < 0) {
                ball.setPosition(new Vector(ball.getPosition().getX(), 0));
                ball.flipAboutAxis(Axis.X);
            }
        }
    }

    void updateAllBallCoefficients() {
        for (Ball ball : balls) {
            ball.setCoefficientOfRestitution(coefficientOfRestitution);
            ball.setDragCoefficient(dragCoefficient);
        }
    }

    void draw() {
        Graphics g = getGraphics();

        Graphics2D graphics = (Graphics2D) backBuffer.getGraphics();
        AffineTransform at = graphics.getTransform();
        graphics.scale(1, -1);
        graphics.translate(0, -windowHeight);

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, windowWidth, windowHeight);

        for (Ball ball : balls) {
            ball.draw(graphics);
        }
        ray.draw(graphics);

        // Reset transform to draw HUD text in normal orientation
        graphics.setTransform(at);

        // Draw physics parameters in upper right corner
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Monospaced", Font.PLAIN, 14));
        String fpsText = String.format("FPS: %.1f", actualFps);
        String restitutionText = String.format("Restitution: %.2f", coefficientOfRestitution);
        String dragText = String.format("Drag: %.3f", dragCoefficient);

        int textX = windowWidth - 150;
        int textY = 20;
        graphics.drawString(fpsText, textX, textY);
        graphics.drawString(restitutionText, textX, textY + 20);
        graphics.drawString(dragText, textX, textY + 40);

        g.drawImage(backBuffer, insets.left, insets.top, this);
    }
}
