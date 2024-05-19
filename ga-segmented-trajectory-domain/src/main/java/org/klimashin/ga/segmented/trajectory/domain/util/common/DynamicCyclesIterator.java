package org.klimashin.ga.segmented.trajectory.domain.util.common;

import static org.klimashin.ga.segmented.trajectory.domain.util.common.DynamicCyclesIterator.State.INTERMEDIATE_ITERATION;
import static org.klimashin.ga.segmented.trajectory.domain.util.common.DynamicCyclesIterator.State.LAST_ITERATION;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DynamicCyclesIterator {

    SortedMap<Integer, Component> components = new TreeMap<>(Integer::compareTo);

    public DynamicCyclesIterator(Map<Integer, Number[]> rawComponents) {
        for (int idx = 0; idx < rawComponents.size(); idx++) {
            var componentAsList = rawComponents.get(idx);

            if (idx == 0) {
                components.put(idx, new Component(componentAsList));
            } else {
                components.put(idx, new Component(componentAsList, components.get(idx - 1)));
            }
        }
    }

    public DynamicCyclesIterator(Map<Integer, Number[]> rawComponents, Map<Integer, Integer> cellRefs) {
        for (int idx = 0; idx < rawComponents.size(); idx++) {
            var componentAsList = rawComponents.get(idx);

            if (idx == 0) {
                components.put(idx, new Component(componentAsList, cellRefs.get(idx)));
            } else {
                components.put(idx, new Component(componentAsList, cellRefs.get(idx), components.get(idx - 1)));
            }
        }
    }

    public void execute(Consumer<Number[]> functionConsumer) {
        var lastComponent = components.lastEntry().getValue();

        while (true) {
            var elementsArray = components.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> {
                        var component = entry.getValue();
                        return component.getCurrentCellRefElement();
                    }).toArray(Number[]::new);

            functionConsumer.accept(elementsArray);

            var state = lastComponent.iterate();
            if (state.equals(LAST_ITERATION)) break;
        }
    }

    public void bulkExecute(Consumer<Number[]> functionConsumer) {
        var threadCount = 6;

        var sumIterations = (int) components.values().stream()
                .map(Component::getElements)
                .map(numbers -> numbers.length)
                .reduce(1, (a, b) -> a * b);
        var counter = 0;

        //Executors.newFixedThreadPool(threadCount))
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var listOfElementsArray = new ArrayList<Number[]>();

            while (true) {
                var startTime = LocalTime.now();

                for (int i = 0; i < threadCount; i++) {
                    var elementsArray = components.entrySet().stream()
                            .sorted(Map.Entry.comparingByKey())
                            .map(entry -> entry.getValue().getCurrentCellRefElement())
                            .toArray(Number[]::new);

                    listOfElementsArray.add(elementsArray);

                    var state = components.lastEntry().getValue().iterate();
                    if (state.equals(LAST_ITERATION)) break;
                }

                var futureList = listOfElementsArray.stream()
                        .map(numbers -> CompletableFuture.runAsync(() -> functionConsumer.accept(numbers), executor))
                        .toArray(CompletableFuture[]::new);

                CompletableFuture.allOf(futureList).get();

                counter += threadCount;

                if (counter >= sumIterations) {
                    break;
                } else {
                    listOfElementsArray.clear();
                }

                var finishTime = LocalTime.now();

                log.info(String.valueOf(Duration.between(startTime, finishTime).toMillis()));
            }

        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected enum State {
        INTERMEDIATE_ITERATION, LAST_ITERATION
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    protected static class Component {

        public Component(Number[] elements) {
            this.previousComponent = null;
            this.elements = elements;
            this.cellRef = 0;
        }

        public Component(Number[] elements, Integer cellRef) {
            this.previousComponent = null;
            this.elements = elements;
            this.cellRef = cellRef;
        }

        public Component(Number[] elements, Component previousComponent) {
            this.previousComponent = previousComponent;
            this.elements = elements;
            this.cellRef = 0;
        }

        public Component(Number[] elements, Integer cellRef, Component previousComponent) {
            this.previousComponent = previousComponent;
            this.cellRef = cellRef;
            this.elements = elements;
        }

        final Component previousComponent;

        @Getter(value = AccessLevel.PROTECTED)
        final Number[] elements;

        int cellRef;

        public Number getCurrentCellRefElement() {
            return this.elements[cellRef];
        }

        public State iterate() {
            this.cellRef++;

            return this.cellRef >= this.elements.length
                    ? this.reset()
                    : INTERMEDIATE_ITERATION;
        }

        public State reset() {
            this.cellRef = 0;

            return previousComponent != null
                    ? this.previousComponent.iterate()
                    : LAST_ITERATION;
        }
    }
}
