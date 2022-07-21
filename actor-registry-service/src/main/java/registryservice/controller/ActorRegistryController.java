package registryservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import registryservice.dto.TrafficLight;
import registryservice.dto.Vehicle;
import registryservice.service.ActorRegistryService;

import java.util.List;

/**
 * ActorRegistry Rest Controller that declares and implements all REST methods and forward these requests to {@link ActorRegistryService}
 */
@RestController
public class ActorRegistryController {

    private static final Logger LOG = LoggerFactory.getLogger(ActorRegistryController.class);

    @Autowired
    private ActorRegistryService actorRegistryService;


    /**
     * Rest POST Method - Inserts new vehicle in db
     *
     * @param vin      of a vehicle that is going to be stored in db
     * @param model    of a vehicle that is going to be stored in db
     * @param producer of a vehicle that is going to be stored in db
     * @return Response entity with response status 200
     */
    @PostMapping(path = "/addVehicle")
    public ResponseEntity addVehicle(@RequestParam String vin,
                                     @RequestParam String model,
                                     @RequestParam String producer) {
        LOG.info("Received POST insert vehicle: " + vin);
        actorRegistryService.addVehicle(vin, model, producer);
        return ResponseEntity.status(HttpStatus.OK).body("");

    }

    /**
     * Rest POST Method - Inserts new traffic light in db
     *
     * @param id        of a traffic light that is going to be stored in db
     * @param longitude of a traffic light that is going to be stored in db
     * @param latitude  of a traffic light that is going to be stored in db
     * @return Response entity with response status 200
     */
    @PostMapping(path = "/addTrafficLight")
    public ResponseEntity addTrafficLight(@RequestParam Long id,
                                          @RequestParam Double longitude,
                                          @RequestParam Double latitude) {
        LOG.info("Received POST add traffic light: " + id);
        actorRegistryService.addTrafficLight(id, longitude, latitude);
        return ResponseEntity.status(HttpStatus.OK).body("");

    }

    /**
     * Rest GET Method - Checks if the vehicle is in radius of some traffic light
     *
     * @param latitude   of checked vehicle
     * @param longitude  of checked vehicle
     * @return Response entity with an id of the found traffic light and response status 200
     */
    @GetMapping(path = "/checkRadius")
    public ResponseEntity<Long> checkRadius(@RequestParam Double latitude, @RequestParam Double longitude) {
        LOG.info("Received check raidus for: " + latitude + " " + longitude);
        return ResponseEntity.ok(actorRegistryService.checkRadius(latitude, longitude));
    }

    /**
     * Rest GET Method - Gets all vehicles from db
     *
     * @return Response entity with a list of all vehicles and response status 200
     */
    @GetMapping(path = "/getAllVehicles")
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        return ResponseEntity.ok(actorRegistryService.getAllVehicles());
    }

    /**
     * Rest GET Method - Gets all traffic lights from db
     *
     * @return Response entity with a list off all traffic lights and response status 200
     */
    @GetMapping(path = "/getAllTrafficLights")
    public ResponseEntity<List<TrafficLight>> getAllTrafficLights() {
        return ResponseEntity.ok(actorRegistryService.getAllTrafficLights());
    }

}
