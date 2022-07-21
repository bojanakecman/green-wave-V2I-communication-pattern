package at.tuwien.dse.apigateway.service;

import at.tuwien.dse.apigateway.dto.TrafficLight;
import at.tuwien.dse.apigateway.dto.Vehicle;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ApiGateway Service that manages Requests from {@link at.tuwien.dse.apigateway.controller.ApiGatewayController}
 */
@Service
public class ApiGatewayService {

    private static final Logger LOG = LoggerFactory.getLogger(ApiGatewayService.class);

    private Client client;
    private ObjectMapper objectMapper;

    public ApiGatewayService() {
        client = ClientBuilder.newClient();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public ApiGatewayService(Client client) {
        this.client = client;
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
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
     * Forwards call to insert vehicle to the actor registry service and then dao
     *
     * @param producer  of a vehicle that is going to be stored in db
     * @param vehicleID of a vehicle that is going to be stored in db
     * @param model     of a vehicle that is going to be stored in db
     * @return Response entity with the status received after vehicle insertion in actor registry service
     */
    public ResponseEntity addVehicle(String producer, String vehicleID, String model) {
        LOG.info("Send REST request to insert new vehicle with id: " + vehicleID);
        String uri = createTargetForRequest("actor-registry-service", 40001, "addVehicle", "");
        Response response = client.target(uri).queryParam("producer", producer).queryParam("vin", vehicleID).queryParam("model", model)
                .request()
                .build("POST")
                .invoke();

        return ResponseEntity.status(response.getStatus()).body("");
    }

    /**
     * Forwards call to insert traffic light to the actor registry service and then dao
     *
     * @param id        of a traffic light that is going to be stored in db
     * @param longitude of a traffic light that is going to be stored in db
     * @param latitude  of a traffic light that is going to be stored in db
     * @return Response entity with the status received after traffic light insertion in actor registry service
     */
    public ResponseEntity addTrafficLight(Double longitude, Double latitude, Long id) {
        LOG.info("Send REST request to insert new traffic light.");
        String uri = createTargetForRequest("actor-registry-service", 40001, "addTrafficLight", "");
        Response response = client.target(uri).queryParam("longitude", longitude)
                .queryParam("latitude", latitude)
                .queryParam("id", id)
                .request()
                .build("POST")
                .invoke();

        return ResponseEntity.status(response.getStatus()).body("");
    }

    /**
     * Forwards call to get all traffic light from actor registry service
     *
     * @return Response entity with a list of all traffic lights and the status received from actor registry service
     */
    public ResponseEntity<List<TrafficLight>> getAllTrafficLights() {
        LOG.info("Send REST request to get all traffic lights");
        String uri = createTargetForRequest("actor-registry-service", 40001, "getAllTrafficLights", "");
        Response response = client.target(uri).request().get();
        return ResponseEntity.status(response.getStatus()).body(parseJsonToList(response.readEntity(String.class), TrafficLight.class));
    }

    /**
     * Forwards call to get all vehicles from actor registry service
     *
     * @return Response entity with a list of all vehicles and the status received from actor registry service
     */
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        LOG.info("Send REST request to get all vehicles");
        String uri = createTargetForRequest("actor-registry-service", 40001, "getAllVehicles", "");
        Response response = client.target(uri).request().get();
        return ResponseEntity.status(response.getStatus()).body(parseJsonToList(response.readEntity(String.class), Vehicle.class));
    }

    /**
     * Parses the request response from String to List
     *
     * @param requestResult response to be parsed
     * @param clazz class in which the response should be parsed to
     * @param <T>
     * @return response as parsed result list
     */
    private <T> List<T> parseJsonToList(String requestResult, Class clazz) {
        LOG.info("Sending request: " + requestResult);
        List<T> resultList = new ArrayList<>();
        try {
            resultList = objectMapper.readValue(requestResult, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("Error while parsing object from String to List.");
        }
        return resultList;
    }


}
