package org.klimashin.ga.segmented.trajectory.domain.util.common;

import org.klimashin.ga.segmented.trajectory.domain.util.component.Point;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Points {

    public static double distanceBetween(Point firstPoint, Point secondPoint) {
        return Math.sqrt(Math.pow(firstPoint.getX() - secondPoint.getX(), 2)
                + Math.pow(firstPoint.getY() - secondPoint.getY(), 2));
    }

    public static Point copy(Point point) {
        return Point.of(point.getX(), point.getY());
    }
}
