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
public class Vector {

    double x;
    double y;

    public static Vector of(double x, double y) {
        return new Vector(x, y);
    }

    public static Vector zero() {
        return new Vector(0, 0);
    }

    public Vector add(Vector vector) {
        this.x += vector.x;
        this.y += vector.y;
        return this;
    }

    public Vector subtract(Vector vector) {
        this.x -= vector.x;
        this.y -= vector.y;
        return this;
    }

    public Vector multiply(double ratio) {
        this.x *= ratio;
        this.y *= ratio;
        return this;
    }

    public Vector divide(double ratio) {
        if (ratio == 0) {
            throw new ArithmeticException("Ошибка при делении вектора на 0");
        }

        this.x /= ratio;
        this.y /= ratio;
        return this;
    }

    public double getScalar() {
        return Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2));
    }

    public Vector toUnit() {
        return divide(getScalar());
    }

    public Vector rotate(double angleInRadian) {
        double newX = (Math.cos(angleInRadian) * this.x) + (-Math.sin(angleInRadian) * this.y);
        double newY = (Math.sin(angleInRadian) * this.x) + (Math.cos(angleInRadian) * this.y);

        this.x = newX;
        this.y = newY;

        return this;
    }

    public Vector copy() {
        return new Vector(this.x, this.y);
    }
}
