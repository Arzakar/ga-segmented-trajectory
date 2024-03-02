package org.klimashin.ga.segmented.trajectory.domain.application.component.entity;

import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBodyName;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "sim_parameters_table")

@Data
@NoArgsConstructor
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SimParametersEntity {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id")
    UUID id;

    @ManyToOne(optional = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @JoinColumn(name = "central_body_name", nullable = false, updatable = false)
    CelestialBodyEntity centralBody;

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

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "engine_fuel_consumption", nullable = false, updatable = false)
    Double engineFuelConsumption;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "engine_thrust", nullable = false, updatable = false)
    Double engineThrust;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "control_variations", nullable = false, updatable = false)
    Map<Integer, Number[][]> controlVariations;

    @OneToOne(cascade = CascadeType.ALL)
    @JdbcTypeCode(SqlTypes.UUID)
    @JoinColumn(name = "last_calculated_sim_id")
    SimResultEntity lastCalculatedSim;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    OffsetDateTime createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, updatable = false)
    OffsetDateTime updatedAt;
}
