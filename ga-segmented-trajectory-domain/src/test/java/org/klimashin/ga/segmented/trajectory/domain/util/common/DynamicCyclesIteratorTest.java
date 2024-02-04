package org.klimashin.ga.segmented.trajectory.domain.util.common;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;

class DynamicCyclesIteratorTest {

    @Test
    void demoTest_zeroStart() {
        var components = Map.of(
                0, new Number[]{10, 20, 30},
                1, new Number[]{50d, 100d, 150d},
                2, new Number[]{10, 20, 30},
                3, new Number[]{50d, 100d, 150d},
                4, new Number[]{10, 20, 30},
                5, new Number[]{50d, 100d, 150d}
        );

        var function = (Consumer<Number[]>) numbers -> System.out.println(Arrays.toString(numbers));
        var iterator = new DynamicCyclesIterator(components);

        iterator.bulkExecute(function);
    }

    @Test
    void demoTest_startFromPoint() {
        var components = Map.of(
                0, new Number[]{10, 20, 30},
                1, new Number[]{50d, 100d, 150d},
                2, new Number[]{10, 20, 30},
                3, new Number[]{50d, 100d, 150d}
        );

        var startPoint = Map.of(
                0, 1,
                1, 0,
                2, 2,
                3, 1
        );

        var function = (Consumer<Number[]>) numbers -> System.out.println(Arrays.toString(numbers));
        var iterator = new DynamicCyclesIterator(components, startPoint);

        iterator.execute(function);
    }

    @Test
    void demoTest_cornerCase() {
        var components = Map.of(
                0, new Number[]{10, 20, 30},
                1, new Number[]{50d, 100d, 150d},
                2, new Number[]{10, 20, 30},
                3, new Number[]{50d, 100d, 150d}
        );

        var startPoint = Map.of(
                0, 2,
                1, 2,
                2, 2,
                3, 2
        );

        var function = (Consumer<Number[]>) numbers -> System.out.println(Arrays.toString(numbers));
        var iterator = new DynamicCyclesIterator(components, startPoint);

        iterator.execute(function);
    }
}