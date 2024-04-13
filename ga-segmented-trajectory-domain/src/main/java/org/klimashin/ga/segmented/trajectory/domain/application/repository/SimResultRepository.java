package org.klimashin.ga.segmented.trajectory.domain.application.repository;

import org.klimashin.ga.segmented.trajectory.domain.application.component.entity.SimResultEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface SimResultRepository extends JpaRepository<SimResultEntity, UUID> {

    @Query("""
            SELECT sr FROM  SimResultEntity sr
            WHERE sr.apocenterAfterLeftGa IS NOT NULL
            AND sr.apocenterAfterRightGa IS NOT NULL
            ORDER BY sr.simParameters.id ASC
            """)
    Stream<SimResultEntity> getApplicableSimResults();
}
