package org.klimashin.ga.segmented.trajectory.domain.model.component.condition;

import org.klimashin.ga.segmented.trajectory.domain.model.component.Particle;
import org.klimashin.ga.segmented.trajectory.domain.util.common.Points;

import lombok.Builder;

@Builder
public record ProximityOfTwoObjects(Particle firstParticle,
                                    Particle secondParticle,
                                    double requiredDistance) implements TargetState {

    public boolean isAchieved() {
        return Points.distanceBetween(firstParticle.getPosition(), secondParticle.getPosition()) < requiredDistance;
    }
}
