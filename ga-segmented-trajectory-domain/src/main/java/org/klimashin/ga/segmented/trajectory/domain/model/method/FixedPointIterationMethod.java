package org.klimashin.ga.segmented.trajectory.domain.model.method;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.function.Function;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FixedPointIterationMethod {

    private static final int maxIterations = 100;

    Function<Double, Double> function;
    double desiredTolerance;

    public double getSolution(double initialGuess) {
        if (initialGuess == 0) {
            throw new RuntimeException("Начальное приближение в методе простой итерации не может быть равным 0");
        }

        var x0 = initialGuess;

        for (int i = 0; i < maxIterations; i++) {
            var x1 = function.apply(x0);

            if (checkDesiredTolerance(x0, x1)) {
                return x1;
            }

            x0 = x1;
        }

        throw new RuntimeException(String.format("Количество итерации метода Ньютона превысило ожидаемое - %d. "
                + "Решение не найдено, возможно уравнение не удовлетворяет условиям сходимости", maxIterations));
    }

    private boolean checkDesiredTolerance(double firstValue, double secondValue) {
        return Math.abs(firstValue - secondValue) <= desiredTolerance;
    }
}
