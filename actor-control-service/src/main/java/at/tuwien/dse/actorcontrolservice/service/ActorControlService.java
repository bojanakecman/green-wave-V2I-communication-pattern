package at.tuwien.dse.actorcontrolservice.service;

import at.tuwien.dse.actorcontrolservice.dao.ActorControlDAO;
import at.tuwien.dse.actorcontrolservice.dto.Movement;
import at.tuwien.dse.actorcontrolservice.dto.TrafficLight;
import at.tuwien.dse.actorcontrolservice.dto.TrafficLightStatus;
import at.tuwien.dse.actorcontrolservice.rabbit.RabbitChannel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DeliverCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ActorControl Service in charge of adjusting speed and time for manual activation of traffic lights
 */
@Service
public class ActorControlService {

    public final static double AVERAGE_RADIUS_OF_EARTH_METERS = 6371000;
    public static final int TRAFFIC_LIGHT_CHANGE = 10;
    private static final Logger LOG = LoggerFactory.getLogger(ActorControlService.class);
    private static final String ACTOR_QUEUE = "actor_queue";

    @Autowired
    private ActorControlDAO actorControlDAO;
    private ObjectMapper objectMapper;
    private Client client;
    private Map<Long, TrafficLight> trafficLights = new HashMap<>();
    private ConcurrentHashMap<Long, TrafficLightStatus> statusMap = new ConcurrentHashMap<>();
    private Map<String, Long> vehicleProvidedSpeed = new HashMap<>();

    public ActorControlService() {
        client = ClientBuilder.newClient();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    }

    public ActorControlService(Client client, ConcurrentHashMap<Long, TrafficLightStatus> statusMap,  Map<Long, TrafficLight> trafficLights, ActorControlDAO actorControlDAO) {
        this.client = client;
        this.statusMap = statusMap;
        this.trafficLights = trafficLights;
        this.actorControlDAO = actorControlDAO;
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @PostConstruct
    public void setUp() {
        //findTrafficLights();
        consumeQueue();
    }

    /**
     * Forwards call to ActorRegistry service to get all traffic lights
     */
    private void findTrafficLights() {
        String uri = constructorURIofResource("actor-registry-service", 40001, "getAllTrafficLights", "");
        Response response = client.target(uri).request().get();
        List<TrafficLight> list = parseJsonToList(response.readEntity(String.class));
        list.forEach(t -> trafficLights.put(t.getId(), t));
    }

    private void consumeQueue() {
        RabbitChannel rabbitChannel = new RabbitChannel();
        DeliverCallback movementCallback = (consumerTag, message) -> {
            String msg = new String(message.getBody(), StandardCharsets.UTF_8);

            if (trafficLights.isEmpty()) {
                findTrafficLights();
            }

            TrafficLightStatus status;
            Movement movement;

            if (("traffic").equals(message.getProperties().getMessageId())) {
                status = objectMapper.readValue(msg, TrafficLightStatus.class);
                statusMap.put(status.getTrafficLightId(), status);
                LOG.info("Traffic Light status read: " + status);

            } else {
                movement = objectMapper.readValue(msg, Movement.class);
                LOG.info("Movement read: " + movement);
                isVehicleInRadius(movement, rabbitChannel);
            }
        };
        try {
            LOG.info(rabbitChannel.toString());
            rabbitChannel.getChannel().basicConsume(ACTOR_QUEUE, true, movementCallback, consumerTag -> {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Forwards call to ActorRegistry service to check if the vehicle is in radius of some traffic light
     *
     * @param movement reported movement
     * @param rabbitChannel for publishing determined speed and manual activation time
     * @throws IOException if an error occurred while trying to determine speed of time for manual traffic light activation
     */
    private void isVehicleInRadius(Movement movement, RabbitChannel rabbitChannel) throws IOException {

        if (statusMap.isEmpty()) return;
        String uri = constructorURIofResource("actor-registry-service", 40001, "checkRadius", "");
        Response response = client.target(uri)
                .queryParam("longitude", movement.getLongitude())
                .queryParam("latitude", movement.getLatitude())
                .request().get();

        Long trafficLight = response.readEntity(Long.class);
        if (trafficLight == 0L) {
            LOG.info("Movement not in radius");
            return;
        }
        if (trafficLight.equals(vehicleProvidedSpeed.get(movement.getVin()))) {
            if (movement.isCrash() && movement.getSpeed() == 0) {
                LOG.info("Movement with crash and speed 0 recognized!");
            } else if (movement.isCrash() && movement.getSpeed() == 50) {
                LOG.info("Movement with crash and speed 50.0 recognized!");
                timeForManualActivation(rabbitChannel, movement, trafficLight);
            } else {
                LOG.info("Traffic light " + trafficLight + " already provided speed to " + movement.getVin());
            }
            return;
        }
        determineSpeed(movement, trafficLight, rabbitChannel);
    }

    /**
     * Determines time for manual activation of a traffic light if the movement reported that crash occurred
     *
     * @param rabbitChannel to publish the determined time to the rabbit queue
     * @param movement detected movement
     * @param trafficLightId of a traffic light that should be manually activated
     * @throws IOException if the value could not be written as String or the value could not be published to the queue
     */
    private void timeForManualActivation(RabbitChannel rabbitChannel, Movement movement, Long trafficLightId) throws IOException {
        TrafficLight trafficLight = trafficLights.get(trafficLightId);
        TrafficLightStatus status = statusMap.get(trafficLightId);

        int distance = calculateDistanceInMeter(movement.getLatitude(), movement.getLongitude(), trafficLight.getLatitude(), trafficLight.getLongitude());
        long secondsPassed = status.getDateTime().until(LocalDateTime.now(), ChronoUnit.SECONDS);
        long secondsLeft = TRAFFIC_LIGHT_CHANGE - secondsPassed;
        long timeUnitTL = (long) (distance / (movement.getSpeed() / 3.6));
        LOG.info("Distance: {}  Speed: {}   Manual time: {}", distance, movement.getSpeed(), timeUnitTL);
        LOG.info("Starting manual determination..");
        // if green and no need for manual traffic light activation
        if (status.isGreen() && secondsLeft > timeUnitTL) {
            LOG.info("Vehicle will made it after NCE event! Current status: Green!");
            return;
        }
        // if red but still getting on green wave with 50 km/h
        if (!status.isGreen() && secondsLeft <= timeUnitTL && (secondsLeft + TRAFFIC_LIGHT_CHANGE) > timeUnitTL) {
            LOG.info("Vehicle will made it after NCE event! Current status: Red!");
            return;
        }
        LOG.info("Set traffic light status manually");
        status.setDateTime(LocalDateTime.now().plusSeconds(timeUnitTL));

        AMQP.BasicProperties messageId = new AMQP.BasicProperties().builder().messageId("traffic").build();
        String msg = objectMapper.writeValueAsString(status);
        rabbitChannel.getChannel().basicPublish("", "speed_queue", messageId, msg.getBytes());

    }

    /**
     * Calculates distance between two location points in meters
     *
     * @param userLat  the latitude of the first location point
     * @param userLng  the longitude of the first location point
     * @param venueLat the latitude of the second location point
     * @param venueLng the longitude og the second location point
     * @return distance in meters
     */
    public int calculateDistanceInMeter(double userLat, double userLng,
                                        double venueLat, double venueLng) {

        double latDistance = Math.toRadians(userLat - venueLat);
        double lngDistance = Math.toRadians(userLng - venueLng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(venueLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (int) (Math.round(AVERAGE_RADIUS_OF_EARTH_METERS * c));
    }

    /**
     * Determines target speed for the vehicle to achieve a green wave
     *
     * @param movement detected movement
     * @param trafficLightId of the traffic light that the vehicle is approaching
     * @param rabbitChannel to publish determined speed to the rabbit queue
     * @throws IOException if the value could not be written as String or the value could not be published to the queue
     */
    public void determineSpeed(Movement movement, Long trafficLightId, RabbitChannel rabbitChannel) throws IOException{
        double speed;
        TrafficLight trafficLight = trafficLights.get(trafficLightId);
        TrafficLightStatus status = statusMap.get(trafficLightId);

        LOG.info("Movement in radius of " + trafficLight.toString());

        LOG.info(status.toString());
        int distance = calculateDistanceInMeter(movement.getLatitude(), movement.getLongitude(), trafficLight.getLatitude(), trafficLight.getLongitude());
        long secondsPassed = status.getDateTime().until(LocalDateTime.now(), ChronoUnit.SECONDS);
        long secondsLeft = TRAFFIC_LIGHT_CHANGE - secondsPassed;

        if (status.isGreen()) {
            LOG.info("Current traffic light status: green");
            // if green try to reach green light with max speed (130 km/h)
            if ((distance / (130.0 / 3.6)) < secondsLeft) {
                speed = 130;
            }
            // else wait for next green light
            else {
                speed = ((double) distance) / (secondsLeft + TRAFFIC_LIGHT_CHANGE);
            }
        } else {
            LOG.info("Current traffic light status: red");
            long minTime = (long) (distance / (130.0 / 3.6));
            // try to reach next green with max speed
            if (minTime > secondsLeft && minTime < (secondsLeft + 10)) {
                speed = 130;
            } else {
                speed = ((double) distance) / (secondsLeft);
            }
        }
        speed *= 3.6;

        LOG.info("Determined speed " + speed);

        // speed can't be greater than 130 and
        // less than 40 if distance to traffic light is still large (> 500m)
        if (speed > 130 || (speed < 40 && distance > 600)) {
            return;
        }

        // save which traffic light provided this vehicle with the speed
        // so that we only have to determine speed once per traffic light
        vehicleProvidedSpeed.put(movement.getVin(), trafficLightId);
        movement.setSpeed(speed);
        String msg = objectMapper.writeValueAsString(movement);
        rabbitChannel.getChannel().basicPublish("", "speed_queue", null, msg.getBytes());
        actorControlDAO.addMovement(movement);
    }

    /**
     * Parses the request response from String to List
     *
     * @param requestResult response to be parsed
     * @param <T>
     * @return response as parsed result list
     */
    private <T> List<T> parseJsonToList(String requestResult) {
        LOG.info("Sending request: " + requestResult);
        List<T> resultList = new ArrayList<>();
        try {
            resultList = objectMapper.readValue(requestResult, objectMapper.getTypeFactory().constructCollectionType(List.class, TrafficLight.class));
        } catch (IOException e) {
            LOG.error("Error while parsing object from String to List.");
        }
        return resultList;
    }

    /**
     * Constructs and returns URI of the request for all REST request in one method
     *
     * @param host
     * @param port
     * @param methodName
     * @param pathParam
     * @return Concatenated string consisting of endpoint
     */
    private String constructorURIofResource(String host, int port, String methodName, String pathParam) {
        StringBuilder stringBuilder = new StringBuilder("http://" + host + ":" + port + "/" + methodName);
        if (!pathParam.isEmpty()) {
            stringBuilder.append("/").append(pathParam);
        }
        return stringBuilder.toString();
    }

}
