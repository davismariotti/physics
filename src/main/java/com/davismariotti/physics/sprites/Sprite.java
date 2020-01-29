package com.davismariotti.physics.sprites;

import com.davismariotti.physics.kinematics.Position;
import com.davismariotti.physics.kinematics.DeltaV;
import com.davismariotti.physics.kinematics.Vector;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.awt.*;

@Data
@AllArgsConstructor
public abstract class Sprite {
    private Position position;
    private Vector vector;
    private DeltaV deltaV;

    public abstract void draw(Graphics2D graphics);

    public abstract void update();
}
