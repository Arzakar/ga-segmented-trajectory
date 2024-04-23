package org.klimashin.ga.segmented.trajectory.domain.util.component;

public class Segment<TYPE extends Comparable<TYPE>> extends Pair<TYPE, TYPE> {

    public static <TYPE extends Comparable<TYPE>> Segment<TYPE> of(TYPE left, TYPE right) {
        return new Segment<>(left, right);
    }

    public Segment(TYPE left, TYPE right) {
        super(left, right);

        if (left.compareTo(right) >= 0) {
            throw new IllegalArgumentException(String.format("left value %s can't be more or equals right value %s", left, right));
        }
    }

    public int compareByLeftTo(Pair<TYPE, TYPE> other) {
        return this.getLeft().compareTo(other.getLeft());
    }

    public int compareByRightTo(Pair<TYPE, TYPE> other) {
        return this.getRight().compareTo(other.getRight());
    }

    public boolean isInclude(TYPE value) {
        return value.compareTo(this.getLeft()) >= 0 && value.compareTo(this.getRight()) <= 0;
    }

    public boolean isIntersectWith(Segment<TYPE> other) {
        if (this.getLeft().compareTo(other.getLeft()) == 0
                || this.getRight().compareTo(other.getRight()) == 0) {
            return true;
        }

        return this.getLeft().compareTo(other.getLeft()) < 0
                ? other.getLeft().compareTo(this.getRight()) < 0
                : this.getLeft().compareTo(other.getRight()) < 0;
    }
}
