package org.klimashin.ga.segmented.trajectory.domain.application.component.entity;

import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBodyName;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "initial")

@Data
@NoArgsConstructor
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InitialEntity {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id")
    UUID id;

    @ManyToOne(optional = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @JoinColumn(name = "central_body_name", nullable = false, updatable = false)
    CelestialBodyEntity centralBody;

    @ManyToOne(optional = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @JoinColumn(name = "celestial_body_name", nullable = false, updatable = false)
    CelestialBodyEntity celestialBody;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "celestial_body_anomaly", nullable = false, updatable = false)
    Double celestialBodyAnomaly;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "spacecraft_pos_x", nullable = false, updatable = false)
    Double spacecraftPosX;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "spacecraft_pos_y", nullable = false, updatable = false)
    Double spacecraftPosY;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "spacecraft_spd_x", nullable = false, updatable = false)
    Double spacecraftSpdX;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "spacecraft_spd_y", nullable = false, updatable = false)
    Double spacecraftSpdY;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "spacecraft_fuel_consumption", nullable = false, updatable = false)
    Double fuelConsumption;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "spacecraft_mass", nullable = false, updatable = false)
    Double mass;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "spacecraft_fuel_mass", nullable = false, updatable = false)
    Double fuelMass;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "spacecraft_thrust", nullable = false, updatable = false)
    Double thrust;

    @JdbcTypeCode(SqlTypes.BIGINT)
    @Column(name = "interval_left_bound", nullable = false, updatable = false)
    Long intervalLeftBound;

    @JdbcTypeCode(SqlTypes.BIGINT)
    @Column(name = "interval_right_bound", nullable = false, updatable = false)
    Long intervalRightBound;

    @JdbcTypeCode(SqlTypes.BIGINT)
    @Column(name = "interval_step", nullable = false, updatable = false)
    Long intervalStep;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "deviation_left_bound", nullable = false, updatable = false)
    Double deviationLeftBound;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "deviation_right_bound", nullable = false, updatable = false)
    Double deviationRightBound;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "deviation_step", nullable = false, updatable = false)
    Double deviationStep;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "required_distance", nullable = false, updatable = false)
    Double requiredDistance;

    @OneToOne(cascade = CascadeType.ALL)
    @JdbcTypeCode(SqlTypes.UUID)
    @JoinColumn(name = "last_calculate_id")
    ResultEntity lastCalculate;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    OffsetDateTime createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, updatable = false)
    OffsetDateTime updatedAt;
}
