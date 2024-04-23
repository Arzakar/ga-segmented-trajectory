package org.klimashin.ga.segmented.trajectory.domain.util.component;

public final class LongSegment extends Segment<Long> {

    public static LongSegment of(Long left, Long right) {
        return new LongSegment(left, right);
    }

    public static LongSegment of(Long left, Integer right) {
        return new LongSegment(left, right);
    }

    public static LongSegment of(Integer left, Long right) {
        return new LongSegment(left, right);
    }

    public static LongSegment of(Integer left, Integer right) {
        return new LongSegment(left, right);
    }

    public LongSegment(Long left, Long right) {
        super(left, right);
    }

    public LongSegment(Long left, Integer right) {
        super(left, right.longValue());
    }

    public LongSegment(Integer left, Long right) {
        super(left.longValue(), right);
    }

    public LongSegment(Integer left, Integer right) {
        super(left.longValue(), right.longValue());
    }
}
