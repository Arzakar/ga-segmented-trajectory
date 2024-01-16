package org.klimashin.ga.segmented.trajectory.domain.application.repository;

import org.klimashin.ga.segmented.trajectory.domain.application.component.entity.OrbitEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrbitRepository extends JpaRepository<OrbitEntity, String> {
}