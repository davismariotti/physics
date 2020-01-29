package com.davismariotti.physics.kinematics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeltaV {

    public static DeltaV ZERO = new DeltaV();
    public static DeltaV GRAVITY = new DeltaV(0, 1);

    int xComponent = 0;
    int yComponent = 0;

    public DeltaV add(DeltaV other) {
        int xComponent = this.xComponent;
        int yComponent = this.yComponent;

        xComponent += other.getXComponent();
        yComponent += other.getYComponent();
        return new DeltaV(xComponent, yComponent);
    }

    public Vector applyToVector(Vector old) {
        Vector vector = new Vector();
        vector.setXComponent(old.getXComponent() + xComponent);
        vector.setYComponent(old.getYComponent() + yComponent);

        return vector;
    }
}
