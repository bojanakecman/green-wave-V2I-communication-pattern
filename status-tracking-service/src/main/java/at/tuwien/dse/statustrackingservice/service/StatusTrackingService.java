package at.tuwien.dse.statustrackingservice.service;

import at.tuwien.dse.statustrackingservice.dao.StatusTrackingDAO;
import at.tuwien.dse.statustrackingservice.dto.Movement;
import at.tuwien.dse.statustrackingservice.dto.TrafficLightStatus;
import at.tuwien.dse.statustrackingservice.rabbit.RabbitChannel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.DeliverCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * StatusTracking Service in charge of receiving/forwarding data from/to ApiGateway service
 */
@Service
public class StatusTrackingService {

    private static final Logger LOG = LoggerFactory.getLogger(StatusTrackingService.class);
    private static final String STATUS_QUEUE = "status_queue";
    private final StatusTrackingDAO statusTrackingDAO;
    private ObjectMapper objectMapper;
    private Client client;


    @Autowired
    public StatusTrackingService(StatusTrackingDAO statusTrackingDAO) {
        client = ClientBuilder.newClient();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        try {
            consumeQueue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.statusTrackingDAO = statusTrackingDAO;
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
    private static String createTargetForRequest(String host, int port, String methodName, String pathParam) {
        StringBuilder stringBuilder = new StringBuilder("http://" + host + ":" + port + "/" + methodName);
        if (!pathParam.isEmpty()) {
            stringBuilder.append("/").append(pathParam);
        }
        return stringBuilder.toString();
    }

    private void consumeQueue() throws IOException {
        RabbitChannel rabbitChannel = new RabbitChannel();
        DeliverCallback movementCallback = (consumerTag, message) -> {
            String msg = new String(message.getBody(), StandardCharsets.UTF_8);
            if (("traffic").equals(message.getProperties().getMessageId())) {
                TrafficLightStatus status = objectMapper.readValue(msg, TrafficLightStatus.class);
                LOG.info("Traffic Light status read: " + status);
                statusTrackingDAO.addTrafficLightStatus(status);
                String uri = createTargetForRequest("api-gateway", 10113, "notifySocketTLStatus", "");
                Response response = client.target(uri).queryParam("green", status.isGreen())
                        .queryParam("id", status.getTrafficLightId())
                        .queryParam("time", status.getDateTime())
                        .request()
                        .build("POST")
                        .invoke();
            } else {
                Movement movement = objectMapper.readValue(msg, Movement.class);
                LOG.info("Movement read: " + movement);
                statusTrackingDAO.addMovement(movement);
                String uri = createTargetForRequest("api-gateway", 10113, "notifySocketMovement", "");
                Response response = client.target(uri).queryParam("vin", movement.getVin())
                        .queryParam("speed", movement.getSpeed())
                        .queryParam("time", movement.getDateTime())
                        .queryParam("longitude", movement.getLongitude())
                        .queryParam("latitude", movement.getLatitude())
                        .queryParam("crash", movement.isCrash())
                        .request()
                        .build("POST")
                        .invoke();
            }

        };
        try {
            LOG.info(rabbitChannel.toString());
            rabbitChannel.getChannel().basicConsume(STATUS_QUEUE, true, movementCallback, consumerTag -> {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
