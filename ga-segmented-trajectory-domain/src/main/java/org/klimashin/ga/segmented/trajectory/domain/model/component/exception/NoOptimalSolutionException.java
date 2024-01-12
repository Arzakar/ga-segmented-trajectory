package org.klimashin.ga.segmented.trajectory.domain.model.component.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NoOptimalSolutionException extends RuntimeException {

    double bestApproach;

    public NoOptimalSolutionException(String message, double bestApproach) {
        super(message);
        this.bestApproach = bestApproach;
    }

    public NoOptimalSolutionException(String message) {
        super(message);
        this.bestApproach = 0;
    }
}
