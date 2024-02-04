package org.klimashin.ga.segmented.trajectory.domain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@SpringBootApplication
@EnableScheduling
public class GaSegmentedTrajectoryDomainApplication {

	public static void main(String[] args) {
		SpringApplication.run(GaSegmentedTrajectoryDomainApplication.class, args);
	}
}
