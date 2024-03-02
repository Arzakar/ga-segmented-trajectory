package org.klimashin.ga.segmented.trajectory.domain.application.component.entity;

import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBodyName;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "sim_results")

@Data
@NoArgsConstructor
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SimResultEntity {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id")
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JdbcTypeCode(SqlTypes.UUID)
    @JoinColumn(name = "sim_parameters_id", nullable = false, updatable = false)
    SimParametersEntity simParameters;

    @JdbcTypeCode(SqlTypes.JSON)
    @JoinColumn(name = "celestial_bodies_by_anomalies", nullable = false, updatable = false)
    Map<CelestialBodyName, Double> celestialBodiesByAnomalies;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "spacecraft_pos", nullable = false, updatable = false)
    Double[] spacecraftPos;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "spacecraft_spd", nullable = false, updatable = false)
    Double[] spacecraftSpd;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "spacecraft_mass", nullable = false, updatable = false)
    Double spacecraftMass;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "spacecraft_fuel_mass", nullable = false, updatable = false)
    Double spacecraftFuelMass;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "control_law", nullable = false, updatable = false)
    Map<Integer, Number[]> controlLaw;

    @JdbcTypeCode(SqlTypes.BOOLEAN)
    @Column(name = "is_meeting_earth", nullable = false, updatable = false)
    Boolean isMeetingEarth;

    @JdbcTypeCode(SqlTypes.BIGINT)
    @Column(name = "e2e_duration", nullable = false, updatable = false)
    Long earthToEarthDuration;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "apocenter_after_ga", updatable = false)
    Double apocenterAfterGa;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    OffsetDateTime createdAt;
}
