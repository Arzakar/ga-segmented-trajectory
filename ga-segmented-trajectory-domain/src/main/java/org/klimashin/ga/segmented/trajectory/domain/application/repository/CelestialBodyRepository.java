package org.klimashin.ga.segmented.trajectory.domain.application.repository;

import org.klimashin.ga.segmented.trajectory.domain.application.component.entity.CelestialBodyEntity;
import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBodyName;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CelestialBodyRepository extends JpaRepository<CelestialBodyEntity, CelestialBodyName> {
}