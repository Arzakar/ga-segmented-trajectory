package org.klimashin.ga.segmented.trajectory.domain.util.common;

import org.klimashin.ga.segmented.trajectory.domain.util.component.Point;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Vector;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Vectors {

    public static Vector between(Point startPoint, Point endPoint) {
        return Vector.of(endPoint.getX() - startPoint.getX(), endPoint.getY() - startPoint.getY());
    }

    public static Vector copy(Vector vector) {
        return Vector.of(vector.getX(), vector.getY());
    }
}
