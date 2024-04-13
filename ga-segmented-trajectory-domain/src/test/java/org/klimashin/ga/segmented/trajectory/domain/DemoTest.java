package org.klimashin.ga.segmented.trajectory.domain;

import org.klimashin.ga.segmented.trajectory.domain.api.dto.SimParametersCreationRequestDto;
import org.klimashin.ga.segmented.trajectory.domain.model.Environment;
import org.klimashin.ga.segmented.trajectory.domain.model.Simulator;
import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBody;
import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBodyName;
import org.klimashin.ga.segmented.trajectory.domain.model.component.Orbit;
import org.klimashin.ga.segmented.trajectory.domain.model.component.Spacecraft;
import org.klimashin.ga.segmented.trajectory.domain.model.component.condition.ProximityOfTwoObjects;
import org.klimashin.ga.segmented.trajectory.domain.model.component.profile.FvdCommandProfile;
import org.klimashin.ga.segmented.trajectory.domain.util.common.Points;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Point;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Vector;

import lombok.SneakyThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

class DemoTest {

    private Environment prepareEnvironment() {
        var solar = CelestialBody.builder()
                .mass(1.9885E+30)
                .build();
        var earth = CelestialBody.builder()
                .mass(5.9722E+24)
                .orbit(Orbit.builder()
                        .attractingBody(solar)
                        .apocenter(1.521E+11)
                        .pericenter(1.471E+11)
                        .semiMajorAxis(149597500000.00)
                        .eccentricity(0.0167086)
                        .inclination(0)
                        .longitudeAscNode(0)
                        .perihelionArgument(0)
                        .trueAnomaly(0)
                        .zeroEpoch(0)
                        .build())
                .name(CelestialBodyName.EARTH)
                .build();
        var spacecraft = Spacecraft.builder()
                .mass(350)
                .position(Point.of(earth.getPosition().getX() - Math.pow(10, 9), 0))
                .speed(Vector.of(0, 29884))
                .fuelMass(200)
                .thrust(0.198 * 2)
                .fuelConsumption(0.000024)
                .build();
        var intervals = Map.of(
                Duration.ofDays(52).toSeconds(), Math.toRadians(-69),
                Duration.ofDays(120).toSeconds(), Math.toRadians(50)
        );
        var commandProfile = new FvdCommandProfile(spacecraft, solar, intervals);
        var targetState = new ProximityOfTwoObjects(spacecraft, earth, Math.pow(10, 9));

        return Environment.builder()
                .centralBody(solar)
                .celestialBodies(Map.of(CelestialBodyName.EARTH, earth))
                .spacecraft(spacecraft)
                .commandProfile(commandProfile)
                .targetState(targetState)
                .build();
    }

    @Test
    void simulationDemo() {
        var environment = prepareEnvironment();
        var modeler = new Simulator(environment);

        var result = modeler.execute(500L);

        System.out.println(result.getSpacecraft().getPosition());
        System.out.println(result.getResultOrbit());
        System.out.println(result.getCurrentTime() / 86400d);
    }

    @Test
    void simulationDemoDetailed() {
        var environment = prepareEnvironment();
        var modeler = new Simulator(environment);

        var result = modeler.detailedExecute(100);

        var numberFormat = "%.3e";

        result.stream().forEachOrdered(env -> {
            var rate = 1;
            var spacecraft = env.getSpacecraft();
            var earth = env.getCelestialBodies().get(CelestialBodyName.EARTH);
            var earthX = String.format(numberFormat, earth.getPosition().getX() / rate);
            var earthY = String.format(numberFormat, earth.getPosition().getY() / rate);
            var scX = String.format(numberFormat, spacecraft.getPosition().getX() / rate);
            var scY = String.format(numberFormat, spacecraft.getPosition().getY() / rate);
            var scV = String.format(numberFormat, spacecraft.getSpeed().getScalar());
            var scMass = String.format(numberFormat, spacecraft.getMass());
            var trueAnomaly = String.format("%.3f", earth.getOrbit().getTrueAnomaly());
            var distance = String.format(numberFormat, Points.distanceBetween(env.getCelestialBodies().get(CelestialBodyName.EARTH).getPosition(), env.getSpacecraft().getPosition()) / rate);

            var report = String.format("%10s   |".repeat(7), earthX, earthY, scX, scY, distance, trueAnomaly,
                    Duration.ofSeconds(env.getTime()).toDays());

            System.out.println(report);
        });
    }

    @Test
    @SneakyThrows
    void mapperTest() {
        var objectMapper = new ObjectMapper();
        var value = SimParametersCreationRequestDto.builder()
                .centralBody(CelestialBodyName.SOLAR)
                .celestialBodiesByAnomalies(new HashMap(Map.of(CelestialBodyName.EARTH, 0d)))
                .spacecraftPos(new Double[]{146100393440d, 0d})
                .spacecraftSpd(new Double[]{0d, 29818.40768009452d})
                .spacecraftMass(1913.9674640888982)
                .spacecraftFuelMass(1009.0621754050131)
                .engineFuelConsumption(0.000043174462795)
                .engineThrust(0.8287589074447651)
                .controlVariations(new HashMap(Map.of(
                        1, new Number[][]{{10, 30},{14.918176034672541, 80.88544644854156}},
                        2, new Number[][]{{70, 170},{113.86908165547607, 179.83635206934508}}
                ))).build();

        System.out.println(objectMapper.writeValueAsString(value));
    }
}
