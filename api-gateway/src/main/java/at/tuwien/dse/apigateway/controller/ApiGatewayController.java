package at.tuwien.dse.apigateway.controller;

import at.tuwien.dse.apigateway.dto.Movement;
import at.tuwien.dse.apigateway.dto.TrafficLight;
import at.tuwien.dse.apigateway.dto.TrafficLightStatus;
import at.tuwien.dse.apigateway.dto.Vehicle;
import at.tuwien.dse.apigateway.service.ApiGatewayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ApiGateway Rest Controller that declares and implements all REST methods and forward these requests to {@link ApiGatewayService}
 */
@RestController
@CrossOrigin(exposedHeaders = "Access-Control-Allow-Origin")
@Api(description = "API Gateway for communication with different microservices")
public class ApiGatewayController {
    private static final Logger LOG = LoggerFactory.getLogger(ApiGatewayController.class);

    @Autowired
    private ApiGatewayService apiGatewayService;

    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    ApiGatewayController(SimpMessagingTemplate template) {
        this.simpMessagingTemplate = template;
    }

    /**
     * Rest POST Method - Forwards call to insert new vehicle
     *
     * @param producer  of a vehicle that is going to be stored in db
     * @param vehicleID of a vehicle that is going to be stored in db
     * @param model     of a vehicle that is going to be stored in db
     * @return Response entity with the status received after vehicle insertion in actor registry service
     */
    @PostMapping(path = "/addVehicle")
    @ApiOperation(value="Add new vehicle")
    public ResponseEntity addVehicle(@RequestParam String producer,
                                     @RequestParam String vehicleID,
                                     @RequestParam String model,
                                     @RequestHeader("id") String headerId) {
        LOG.info("Received POST insert vehicle with id: " + vehicleID);
        return apiGatewayService.addVehicle(producer, vehicleID, model);
    }

    /**
     * Sends traffic light status to Web Socket
     *
     * @param id    of the traffic light
     * @param green status of the traffic light (GREEN => true, RED => false)
     * @param time  timestamp of the next status change
     */
    @PostMapping(path = "/notifySocketTLStatus")
    @ApiOperation(value = "Notify frontend application about changed traffic light status")
    public void sendTLStatusToSocket(@RequestParam Long id,
                                    @RequestParam Boolean green,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time) {
        LOG.info("Get " + id);
        TrafficLightStatus trafficLightStatus = new TrafficLightStatus(green, id, time);
        this.simpMessagingTemplate.convertAndSend("/trafficLights", trafficLightStatus);
    }

    /**
     * Sends movement to Web Socket
     *
     * @param vin       of the vehicle
     * @param speed     of the vehicle
     * @param time      timestamp when the movement happened
     * @param longitude of the vehicle
     * @param latitude  of the vehicle
     * @param crash     status if the crash happened
     */
    @PostMapping(path = "/notifySocketMovement")
    @ApiOperation(value = "Notify frontend application about new movement")
    public void sendMovementToSocket(@RequestParam String vin,
                                     @RequestParam Double speed,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
                                     @RequestParam Double longitude,
                                     @RequestParam Double latitude,
                                     @RequestParam Boolean crash) {
        LOG.info("Sending movement to socket" + vin);
        Movement movement = new Movement();
        movement.setVin(vin);
        movement.setSpeed(speed);
        movement.setDateTime(time);
        movement.setLongitude(longitude);
        movement.setLatitude(latitude);
        movement.setCrash(crash);
        this.simpMessagingTemplate.convertAndSend("/movements", movement);
    }

    /**
     * Rest GET Method - Forwards call to get all vehicles from db
     *
     * @return Response entity with a list of all vehicles and the status received from actor registry service
     */
    @GetMapping(path = "/getAllVehicles")
    @ApiOperation(value = "View list of all vehicles")
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        LOG.info("Received GET all vehicles");
        return apiGatewayService.getAllVehicles();
    }

    /**
     * Rest POST Method - Forwards call to insert new traffic light
     *
     * @param id        of a traffic light that is going to be stored in db
     * @param longitude of a traffic light that is going to be stored in db
     * @param latitude  of a traffic light that is going to be stored in db
     * @return Response entity with the status received after traffic light insertion in actor registry service
     */
    @PostMapping(path = "/addTrafficLight")
    @ApiOperation(value = "Add traffic light")
    public ResponseEntity addTrafficLight(@RequestParam Double longitude,
                                          @RequestParam Double latitude,
                                          @RequestParam Long id) {
        LOG.info("Received POST insert traffic light");
        return apiGatewayService.addTrafficLight(longitude, latitude, id);
    }

    /**
     * Rest GET Method - Forward call to get all traffic lights from db
     *
     * @return Response entity with a list of all traffic lights and the status received from actor registry service
     */
    @GetMapping(path = "/getAllTrafficLights")
    @ApiOperation(value = "View list of all traffic lights")
    public ResponseEntity<List<TrafficLight>> getAllTrafficLights() {
        LOG.info("Recieved GET all traffic lights");
        return apiGatewayService.getAllTrafficLights();
    }

}
