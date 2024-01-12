package org.klimashin.ga.segmented.trajectory.domain.model.component.condition;

import org.klimashin.ga.segmented.trajectory.domain.model.component.MassParticle;
import org.klimashin.ga.segmented.trajectory.domain.util.common.Points;

public record ProximityOfTwoObjects(MassParticle firstParticle,
                                    MassParticle secondParticle,
                                    double requiredDistance) implements TargetState {

    public boolean isAchieved() {
        return Points.distanceBetween(firstParticle.getPosition(), secondParticle.getPosition()) < requiredDistance;
    }
}
