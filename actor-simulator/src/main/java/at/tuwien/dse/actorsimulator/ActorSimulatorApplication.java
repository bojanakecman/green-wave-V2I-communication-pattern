package at.tuwien.dse.actorsimulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class ActorSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActorSimulatorApplication.class, args);

    }


}
