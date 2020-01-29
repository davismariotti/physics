package com.davismariotti.physics.kinematics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Position {
    private int x = 0;
    private int y = 0;

    public Position move(Direction direction) {
        int x = this.x;
        int y = this.y;
        switch (direction) {
            case UP:
                y += 1;
                break;
            case DOWN:
                y -= 1;
                break;
            case LEFT:
                x -= 1;
                break;
            case RIGHT:
                x += 1;
                break;
        }
        return new Position(x, y);
    }

    public Position add(Position other) {
        return new Position(other.getX() + this.x, other.getY() + this.y);
    }

    public boolean isOutOfFrame(int windowWidth, int windowHeight) {
        return x > windowWidth || x < 0 || y > windowHeight || y < 0;
    }
}
