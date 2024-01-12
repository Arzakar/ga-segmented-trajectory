package org.klimashin.ga.segmented.trajectory.domain.model.component.profile;

import org.klimashin.ga.segmented.trajectory.domain.util.component.Vector;

public interface CommandProfile {

    Vector getThrustForceDirection(long currentTime);

    Vector getThrustForce(double thrust, long currentTime);
}
