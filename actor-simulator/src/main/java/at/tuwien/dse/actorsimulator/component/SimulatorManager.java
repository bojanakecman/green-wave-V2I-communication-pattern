package at.tuwien.dse.actorsimulator.component;

import at.tuwien.dse.actorsimulator.dto.Movement;
import at.tuwien.dse.actorsimulator.dto.TrafficLightStatus;
import at.tuwien.dse.actorsimulator.dto.Vehicle;
import at.tuwien.dse.actorsimulator.rabbit.RabbitChannel;
import at.tuwien.dse.actorsimulator.service.SimulatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.DeliverCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ManagedBean
public class SimulatorManager {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatorManager.class);
    private ExecutorService pool = Executors.newFixedThreadPool(3);
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledExecutorService crashScheduler = Executors.newScheduledThreadPool(3);


    private SimulatorService simulatorService;
    private List<Movement> movements;
    private List<Vehicle> vehicles;
    private List<TrafficLightStatus> trafficLightStatuses;

    private RabbitChannel rabbitChannel;
    private ObjectMapper objectMapper;

    @Autowired
    public SimulatorManager(SimulatorService simulatorService) throws IOException {
        this.rabbitChannel = new RabbitChannel();
        this.simulatorService = simulatorService;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.vehicles = new ArrayList<>();
        this.movements = new ArrayList<>();
        this.trafficLightStatuses = new ArrayList<>();

    }

    @PostConstruct
    public void setUp() {
        consumeQueue();
        createVehicles();
        createTrafficLights();
        try {
            // wait for frontend to load core data before real simulation
            Thread.sleep(3000);
            // periodically send traffic light status
            scheduler.scheduleAtFixedRate(new StatusScheduler(trafficLightStatuses, rabbitChannel), 0, 10, TimeUnit.SECONDS);
            Thread.sleep(3000);
            readRoute();
            // start simulation for each vehicle in separate thread
            pool.execute(new SimulationThread(vehicles.get(0), movements, rabbitChannel));
            Thread.sleep(15000);
            pool.execute(new SimulationThread(vehicles.get(1), movements, rabbitChannel));
            Thread.sleep(15000);
            pool.execute(new SimulationThread(vehicles.get(2), movements, rabbitChannel));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void createVehicles() {
        saveVehicle("A11-T-111", "C4", "Citroen", 70.0);
        saveVehicle("B22-E-222", "3", "Bmw", 80.0);
        saveVehicle("C33-K-333", "E550", "Mercedes", 90.0);
    }

    private void createTrafficLights() {
        saveTrafficLight(16.34912, 48.16845, 1L, true);
        saveTrafficLight(16.34548, 48.16265, 2L, false);
        saveTrafficLight(16.33786, 48.15213, 3L, true);
    }

    private void consumeQueue() {
        DeliverCallback speedCallback = (consumerTag, message) -> {
            String msg = new String(message.getBody(), StandardCharsets.UTF_8);
            TrafficLightStatus status;
            Movement movement;
            // message from queue addresses published traffic light status
            if (("traffic").equals(message.getProperties().getMessageId())) {
                status = objectMapper.readValue(msg, TrafficLightStatus.class);
                TrafficLightStatus stati = trafficLightStatuses.stream().filter(s -> s.getTrafficLightId().equals(status.getTrafficLightId()))  //
                        .findFirst().orElse(null);
                long time = LocalDateTime.now().until(status.getDateTime(), ChronoUnit.SECONDS);
                if (stati != null) {
                    LOG.info("Starting scheduler for manual status change");
                    crashScheduler.schedule(new StatusScheduler(stati, rabbitChannel), time, TimeUnit.SECONDS);
                }
                LOG.info("Traffic Light status read for NCE event: " + status);
            } else {
                // message from queue addresses published movement
                movement = objectMapper.readValue(msg, Movement.class);
                vehicles.stream().filter(i -> i.getVin().equals(movement.getVin())).findFirst() //
                        .ifPresent(v -> v.setSpeed(movement.getSpeed()));
                LOG.info("Speed read " + movement.getSpeed());
            }
        };
        try {
            LOG.info(rabbitChannel.toString());
            rabbitChannel.getChannel().basicConsume("speed_queue", true, speedCallback, consumerTag -> {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads movement route used for simulation from txt file
     *
     * @throws IOException id could not read from file
     */
    private void readRoute() throws IOException {
        ClassPathResource resource = new ClassPathResource("route.txt");
        InputStream inputStream = resource.getInputStream();
        InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader);
        for (String line; (line = reader.readLine()) != null; ) {
            // Process line
            String[] arrayLine = line.split(",");
            Movement movement = new Movement();
            movement.setLongitude(Double.parseDouble(arrayLine[0]));
            movement.setLatitude(Double.parseDouble(arrayLine[1]));
            movement.setDistance(Double.parseDouble(arrayLine[2]));
            if (arrayLine.length > 3 && arrayLine[3].equals("NCE")) {
                movement.setCrash(true);
            }
            this.movements.add(movement);
        }
    }

    /**
     * Forwards call to SimulatorService to invoke call to ActorRegistry to save vehicle
     *
     * @param vin of a vehicle to be saved
     * @param model of a vehicle to be saved
     * @param producer of a vehicle to be saved
     * @param speed of a vehicle to be saved
     */
    private void saveVehicle(String vin, String model, String producer, Double speed) {
        Vehicle v = new Vehicle(vin, model, producer);
        v.setSpeed(speed);
        this.simulatorService.saveVehicle(vin, model, producer);
        vehicles.add(v);
    }

    /**
     * Forwards call to SimulatorService to invoke call to ActorRegistry to save traffic light
     *
     * @param longitude of a traffic light to be saved
     * @param latitude of a traffic light to be saved
     * @param id of a traffic light to be saved
     * @param green initial status of a traffic light to be saved (GREEN => true, RED => false)
     */
    private void saveTrafficLight(Double longitude, Double latitude, Long id, boolean green) {
        this.simulatorService.saveTrafficLight(longitude, latitude, id);
        trafficLightStatuses.add(new TrafficLightStatus(green, id, LocalDateTime.now()));
    }


}
