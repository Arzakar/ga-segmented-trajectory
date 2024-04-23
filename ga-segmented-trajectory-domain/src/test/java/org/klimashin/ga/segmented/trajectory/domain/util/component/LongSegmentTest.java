package org.klimashin.ga.segmented.trajectory.domain.util.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LongSegmentTest {

    @Test
    void create_shouldThrowException_whenLeftMoreOrEqualsRight() {
        var exception = catchThrowableOfType(() -> new LongSegment(10L, 5L), IllegalArgumentException.class);

        assertThat(exception.getMessage()).isNotNull();
    }

    @ParameterizedTest
    @CsvSource({
            "-2,false",
            "0,true", "2,true", "5,true",
            "10,false"
    })
    void isInclude_shouldReturnResult(Long value, boolean expectedResult) {
        var segment = LongSegment.of(0, 5);

        assertThat(segment.isInclude(value)).isEqualTo(expectedResult);
    }

    @Test
    void isIntersectWith_shouldReturnTrue_whenLeftBoundaryIsSimilar() {
        var segment = LongSegment.of(0, 5);
        var otherSegment = LongSegment.of(0, 6);

        assertThat(segment.isIntersectWith(otherSegment)).isTrue();
    }

    @Test
    void isIntersectWith_shouldReturnTrue_whenRightBoundaryIsSimilar() {
        var segment = LongSegment.of(0, 5);
        var otherSegment = LongSegment.of(3, 5);

        assertThat(segment.isIntersectWith(otherSegment)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"3,true", "7,false"})
    void isIntersectWith_shouldReturnResult_whenOtherOnRight(Long otherLeftBound, boolean expectedResult) {
        var segment = LongSegment.of(0, 5);
        var otherSegment = LongSegment.of(otherLeftBound, 10);

        assertThat(segment.isIntersectWith(otherSegment)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({"1,true", "-1,false"})
    void isIntersectWith_shouldReturnResult_whenOtherOnLeft(Long otherRightBound, boolean expectedResult) {
        var segment = LongSegment.of(0, 5);
        var otherSegment = LongSegment.of(-5, otherRightBound);

        assertThat(segment.isIntersectWith(otherSegment)).isEqualTo(expectedResult);
    }
}