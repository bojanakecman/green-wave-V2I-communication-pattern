package at.tuwien.dse.actorsimulator.component;

import at.tuwien.dse.actorsimulator.dto.Movement;
import at.tuwien.dse.actorsimulator.dto.Vehicle;
import at.tuwien.dse.actorsimulator.rabbit.RabbitChannel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Thread that implement Runnable interface and executes movement simulation for each registered vehicle
 */
public class SimulationThread implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(SimulationThread.class);
    private static final String MOVEMENT_STATUS_EXCHANGE = "movement_status";

    private Vehicle vehicle;
    private List<Movement> movements;
    private RabbitChannel rabbitChannel;
    private ObjectMapper objectMapper;

    public SimulationThread(Vehicle vehicle, List<Movement> movements, RabbitChannel rabbitChannel) {
        this.vehicle = vehicle;
        this.movements = movements;
        this.rabbitChannel = rabbitChannel;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    public void run() {
        for (Movement movement : movements) {
            try {
                movement.setSpeed(vehicle.getSpeed());
                movement.setVin(vehicle.getVin());
                movement.setDateTime(LocalDateTime.now());
                // vehicle sends NCE event
                if (movement.isCrash()) {
                    movement.setSpeed(0);
                    vehicle.setSpeed(0.0);
                    LOG.info("NCE Event recognized!");
                }
                String msg = objectMapper.writeValueAsString(movement);
                LOG.info("Getting vehicle's speed: " + vehicle.getSpeed());
                rabbitChannel.getChannel().basicPublish(MOVEMENT_STATUS_EXCHANGE, "", null, msg.getBytes());
                // movement still has crash/NCE flag so that backend can apply custom logic
                if (movement.isCrash()) {
                    Thread.sleep(20000);
                    movement.setSpeed(50.0);
                    movement.setVin(vehicle.getVin());
                    movement.setDateTime(LocalDateTime.now());
                    vehicle.setSpeed(50.0);
                    msg = objectMapper.writeValueAsString(movement);
                    LOG.info("Getting vehicle's speed: " + vehicle.getSpeed());
                    rabbitChannel.getChannel().basicPublish(MOVEMENT_STATUS_EXCHANGE, "", null, msg.getBytes());
                }
                double speed = movement.getSpeed() / 3.6;
                // time until next movement depends on the distance from the next location point
                long timeToWait = (long) (movement.getDistance() / speed * 1000);
                Thread.sleep(timeToWait);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
