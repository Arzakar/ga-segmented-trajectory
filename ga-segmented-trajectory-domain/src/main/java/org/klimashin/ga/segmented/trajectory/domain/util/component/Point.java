package org.klimashin.ga.segmented.trajectory.domain.util.component;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Point {

    double x;
    double y;

    public static Point of(double x, double y) {
        return new Point(x, y);
    }

    public static Point of(double[] coords) {
        if (coords.length != 2) {
            throw new IllegalArgumentException("Массив координат должен содержать строго два элемента");
        }

        return new Point(coords[0], coords[1]);
    }

    public Point move(Vector vector) {
        this.x += vector.getX();
        this.y += vector.getY();
        return this;
    }

    public Point copy() {
        return new Point(this.x, this.y);
    }

    public Vector asRadiusVector() {
        return new Vector(this.x, this.y);
    }

    public double[] toArray() {
        return new double[]{this.x, this.y};
    }
}
