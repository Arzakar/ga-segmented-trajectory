package org.klimashin.ga.segmented.trajectory.domain.model.method;

import java.util.function.Function;

//TODO: заменить классы исключений
public class NewtonsMethod {

    Function<Double, Double> function;
    Function<Double, Double> derivative;
    double desiredTolerance;

    static int maxIterations = 100;
    static double minAbsApplicableDerivativeValue = 0.000000000001;

    public double getSolution(double initialGuess) {
        var x0 = initialGuess;

        for (int i = 0; i < maxIterations; i++) {
            var x1 = solutionFunction(x0);

            if (checkDesiredTolerance(x0, x1)) {
                return x1;
            }

            x0 = x1;
        }

        throw new RuntimeException(String.format("Количество итерации метода Ньютона превысило ожидаемое - %d. "
                + "Решение не найдено, возможно уравнение не удовлетворяет условиям сходимости", maxIterations));
    }

    private double solutionFunction(double value) {
        var derivative = this.derivative.apply(value);

        if (Math.abs(derivative) < minAbsApplicableDerivativeValue) {
            throw new RuntimeException("Величина производной слишком мала для применения метода Ньютона");
        }

        return value - function.apply(value) / derivative;
    }

    private boolean checkDesiredTolerance(double firstValue, double secondValue) {
        return Math.abs(firstValue - secondValue) <= desiredTolerance;
    }
}
