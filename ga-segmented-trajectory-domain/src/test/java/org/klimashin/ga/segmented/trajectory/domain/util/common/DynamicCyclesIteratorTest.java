package org.klimashin.ga.segmented.trajectory.domain.util.common;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@ExtendWith(MockitoExtension.class)
class DynamicCyclesIteratorTest {

    @Mock
    Consumer<Number[]> function;

    @Test
    void execute_shouldExecuteAllIterations() {
        var components = Map.of(
                0, new Number[]{10, 20, 30},
                1, new Number[]{50d, 100d},
                2, new Number[]{1, 2, 3}
        );

        new DynamicCyclesIterator(components).execute(function);

        verify(function, times(18)).accept(any());
    }

    @Test
    void execute_shouldExecuteFromPoint() {
        var components = Map.of(
                0, new Number[]{10, 20, 30},
                1, new Number[]{50d, 100d},
                2, new Number[]{1, 2, 3}
        );

        var startPoint = Map.of(
                0, 1,
                1, 0,
                2, 2
        );

        new DynamicCyclesIterator(components, startPoint).execute(function);

        verify(function, times(10)).accept(any());

        verify(function, never()).accept(new Number[]{20, 50d, 2});
        verify(function).accept(new Number[]{20, 50d, 3});
    }
}