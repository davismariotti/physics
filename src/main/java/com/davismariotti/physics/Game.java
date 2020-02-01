package com.davismariotti.physics;

import com.davismariotti.physics.kinematics.Axis;
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
            long time = System.currentTimeMillis();

            if (pressed.size() >= 1) {
                for (int pressedCode : pressed) {
                    if (pressedCode == KeyEvent.VK_LEFT) {
                        ray.addAngle(3);
                    }
                    if (pressedCode == KeyEvent.VK_RIGHT) {
                        ray.addAngle(-3);
                    }
                    if (pressedCode == KeyEvent.VK_ENTER) {
                        Ball ball = new Ball(ray.getPosition(), ray.getUnitVector().multiply(40), Collections.singletonList(GRAVITY));
                        balls.listIterator().add(ball);
                    }
                    if (pressedCode == KeyEvent.VK_Q) {
                        System.exit(0);
                    }
                }
            }

            update();
            draw();

            //  delay for each frame  -   time it took for one frame
            time = (1000 / fps) - (System.currentTimeMillis() - time);

            if (time > 0) {
                try {
                    Thread.sleep(time);
                } catch (Exception ignored) {
                }
            }
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

            if (ball.getPosition().getX() > windowWidth / SCALE || ball.getPosition().getX() < 0) {
                ball.flipAboutAxis(Axis.Y);
            }
            if (ball.getPosition().getY() > windowHeight / SCALE || ball.getPosition().getY() < 0) {
                ball.flipAboutAxis(Axis.X);
            }

//            if (ball.getPosition().isOutOfFrame(windowWidth, windowHeight, 20)) {
//                it.remove();
//            }
        }
        if (balls.size() > 0) {
            System.out.println(balls.get(0).getTotalEnergy());
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

        g.drawImage(backBuffer, insets.left, insets.top, this);
        graphics.setTransform(at);
    }
}
