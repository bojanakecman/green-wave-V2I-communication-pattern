package at.tuwien.dse.actorsimulator.service;

import at.tuwien.dse.actorsimulator.rabbit.RabbitChannel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

/**
 * Simulator Service in charge of simulating data and forwarding data to ApiGateway service
 */
@Service
public class SimulatorService {

    private final static String MOVEMENT_QUEUE = "movement_queue";
    private RabbitChannel rabbitChannel;
    private ObjectMapper objectMapper;
    private Client client;

    @Autowired
    public SimulatorService() {
        client = ClientBuilder.newClient();
    }

    /**
     * Forwards call to ApiGateway service to get all vehicles
     *
     * @param client
     */
    private static void getVehicles(Client client) {
        String uri = createTargetForRequest("api-gateway", 10113, "getAllVehicles", "");
        Response response = client.target(uri)
                .request()
                .build("GET")
                .invoke();
        System.out.println(response);
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

    /**
     * Forwards call to ApiGateway service to save new vehicle
     *
     * @param vehicleId of a vehicle to be inserted in db
     * @param model     of a vehicle to be inserted in db
     * @param producer  of a vehicle to be inserted in db
     */
    public void saveVehicle(String vehicleId, String model, String producer) {
        String uri = createTargetForRequest("api-gateway", 10113, "addVehicle", "");
        this.client.target(uri)
                .queryParam("vehicleID", vehicleId)
                .queryParam("producer", producer)
                .queryParam("model", model)
                .request().header("id", producer)
                .build("POST")
                .invoke();
    }

    /**
     * Forwards call to ApiGateway service to save new traffic light
     *
     * @param longitude  of a traffic light to be inserted to db
     * @param latitude   of a traffic light to be inserted to db
     * @param id         of a traffic light to be inserted to db
     */
    public void saveTrafficLight(Double longitude, Double latitude, Long id) {
        String uri = createTargetForRequest("api-gateway", 10113, "addTrafficLight", "");
        this.client.target(uri)
                .queryParam("longitude", longitude)
                .queryParam("latitude", latitude)
                .queryParam("id", id)
                .request()
                .build("POST")
                .invoke();
    }
}
