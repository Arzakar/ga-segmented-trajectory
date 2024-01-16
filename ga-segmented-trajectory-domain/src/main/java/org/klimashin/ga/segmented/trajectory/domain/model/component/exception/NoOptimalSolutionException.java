package org.klimashin.ga.segmented.trajectory.domain.model.component.exception;

import org.klimashin.ga.segmented.trajectory.domain.model.Environment;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NoOptimalSolutionException extends RuntimeException {

    Environment environment;
    double bestApproach;

    public NoOptimalSolutionException(String message, double bestApproach, Environment environment) {
        super(message);
        this.environment = environment;
        this.bestApproach = bestApproach;
    }

    public NoOptimalSolutionException(String message) {
        super(message);
        this.environment = null;
        this.bestApproach = 0;
    }

    public NoOptimalSolutionException(Environment environment) {
        super();
        this.environment = environment;
        this.bestApproach = 0;
    }
}
