package org.klimashin.ga.segmented.trajectory.domain.application.repository;

import org.klimashin.ga.segmented.trajectory.domain.application.component.entity.ResultEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface ResultRepository extends JpaRepository<ResultEntity, UUID> {
}