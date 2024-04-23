package org.klimashin.ga.segmented.trajectory.domain.model.component;

import org.klimashin.ga.segmented.trajectory.domain.util.component.Point;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Vector;

public interface Particle {

    Point getPosition();

    Vector getSpeed();

    double getMass();
}
