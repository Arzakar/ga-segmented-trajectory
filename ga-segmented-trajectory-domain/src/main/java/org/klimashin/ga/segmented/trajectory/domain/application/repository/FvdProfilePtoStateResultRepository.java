package org.klimashin.ga.segmented.trajectory.domain.application.repository;

import org.klimashin.ga.segmented.trajectory.domain.application.component.entity.FvdProfilePtoStateResultEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FvdProfilePtoStateResultRepository extends JpaRepository<FvdProfilePtoStateResultEntity, UUID> {
}
