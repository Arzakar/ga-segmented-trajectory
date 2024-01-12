package org.klimashin.ga.segmented.trajectory.domain.util.component;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ComparablePair<L extends Comparable<L>, R extends Comparable<R>> {

    L left;
    R right;

    public static <L extends Comparable<L>, R extends Comparable<R>> ComparablePair<L, R> of(L left, R right) {
        return new ComparablePair<>(left, right);
    }

    public int compareByLeftTo(ComparablePair<L, R> pair) {
        return left.compareTo(pair.getLeft());
    }

    public int compareByRightTo(ComparablePair<L, R> pair) {
        return right.compareTo(pair.getRight());
    }
}
