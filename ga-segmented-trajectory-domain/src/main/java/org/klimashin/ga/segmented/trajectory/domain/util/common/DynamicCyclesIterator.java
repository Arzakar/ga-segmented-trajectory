package org.klimashin.ga.segmented.trajectory.domain.util.common;

import static org.klimashin.ga.segmented.trajectory.domain.util.common.DynamicCyclesIterator.State.INTERMEDIATE_ITERATION;
import static org.klimashin.ga.segmented.trajectory.domain.util.common.DynamicCyclesIterator.State.LAST_ITERATION;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.IntStream;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DynamicCyclesIterator {

    /**
     * Список компонентов для итерирования. Ключ карты говорит об уровне слоя в общей схеме циклов, значение карты содержит
     * класс {@link Component} для хранения элементов которые будут использоваться внутри цикла и вспомогательных ссылок.
     * <p/>
     * Структура вида:
     * <pre>
     *     {
     *         0: {
     *             "elements": [1, 2, 3, 4, 5],
     *             "cellRef": 0
     *         },
     *         1: {
     *             "previousComponent": {
     *                 "elements": [1, 2, 3, 4, 5],
     *                 "cellRef": 0
     *             }
     *             "elements": [5, 10, 15, 20],
     *             "cellRef": 0
     *         }
     *     }
     * </pre>
     * будет аналогична вложенному циклу:
     * <pre>
     *     for (int i = 1; i <= 5; i++) {
     *         for (int j = 5; j <= 20; j += 5) {
     *             ...
     *         }
     *     }
     * </pre>
     */
    SortedMap<Integer, Component> components = new TreeMap<>(Integer::compareTo);

    /**
     * Конструктор, для формирования компонентов для итерирования. Самый верхний компонент не имеет ссылок (например,
     * на последний элемент), чтобы не создавать зацикливание.
     *
     * @param rawComponents массивы для выстраивания структуры циклов
     */
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

    /**
     * Конструктор, для формирования компонентов для итерирования. Самый верхний компонент не имеет ссылок (например,
     * на последний элемент), чтобы не создавать зацикливание. В результате получаем итератор, который начинает отсчёт с
     * указанных элементов на каждом уровне
     *
     * @param rawComponents массивы для выстраивания структуры циклов
     * @param cellRefs индексы элементов, с которых нужно начать итерирования на каждом уровне
     */
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

        do {
            var elementsArray = components.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> {
                        var component = entry.getValue();
                        return component.getCurrentCellRefElement();
                    }).toArray(Number[]::new);

            functionConsumer.accept(elementsArray);

        } while(lastComponent.iterate().equals(INTERMEDIATE_ITERATION));
    }

    public void bulkExecute(int permits, Consumer<Number[]> functionConsumer) {
        var sumIterations = (int) components.values().stream()
                .map(Component::getElements)
                .map(numbers -> numbers.length)
                .reduce(1, (a, b) -> a * b);

        try (var scope = new ConstrainedScope<>(permits)) {
            IntStream.range(0, sumIterations).forEachOrdered(_ -> {
                var elementsArray = components.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(entry -> entry.getValue().getCurrentCellRefElement())
                        .toArray(Number[]::new);

                scope.fork(() -> {
                    functionConsumer.accept(elementsArray);
                    return null;
                });

                components.lastEntry().getValue().iterate();
            });

            scope.join().throwIfFailed();

        } catch (ExecutionException | InterruptedException exception) {
            throw new RuntimeException(exception);
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

            while (counter < sumIterations) {
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
                listOfElementsArray.clear();
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

        final Component previousComponent;

        @Getter(value = AccessLevel.PROTECTED)
        final Number[] elements;

        int cellRef;

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
