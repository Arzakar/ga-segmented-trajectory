package org.klimashin.ga.segmented.trajectory.domain.model.method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.function.Function;

class FixedPointIterationMethodTest {

    @ParameterizedTest
    @CsvSource({
            "0.1,0.628199454",
            "0.2,0.628298759",
            "0.3,0.628525059"
    })
    void getSolution_shouldReturnSolution(double initialGuess, double expectedValue) {
        Function<Double, Double> function = (variable) -> 0.9 * Math.sin(variable) + 0.1;

        var method = new FixedPointIterationMethod(function, 0.001);

        assertThat(method.getSolution(initialGuess)).isEqualTo(expectedValue, withPrecision(10E-9));
    }
}