package registryservice.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import registryservice.dao.ActorRegistryDAO;
import registryservice.dto.TrafficLight;
import registryservice.dto.Vehicle;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * ActorRegistry Service that manages Requests from {@link registryservice.controller.ActorRegistryController}, process and forward them to {@link ActorRegistryDAO}
 */
@Service
public class ActorRegistryService {

    private static final Logger LOG = LoggerFactory.getLogger(ActorRegistryService.class);

    @Autowired
    private ActorRegistryDAO actorRegistryDAO;
    private ObjectMapper objectMapper;

    @PostConstruct
    private void postConstruct() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    /**
     * Inserts new vehicle in db
     *
     * @param vin      of a vehicle that is going to be stored in db
     * @param model    of a vehicle that is going to be stored in db
     * @param producer of a vehicle that is going to be stored in db
     */
    public void addVehicle(String vin, String model, String producer) {
        LOG.info("Inserting new Vehicle with ID: " + vin);
        Vehicle vehicle = new Vehicle(vin, model, producer);
        actorRegistryDAO.addVehicle(vehicle);
    }

    /**
     * Gets all vehicles from db
     *
     * @return A list of all vehicles
     */
    public List<Vehicle> getAllVehicles() {
        LOG.info("Getting all vehicles");
        return actorRegistryDAO.getAllVehicles();
    }

    /**
     * Checks if the vehicle is in radius of some traffic light
     *
     * @param latitude   of checked vehicle
     * @param longitude  of checked vehicle
     * @return An id of the found traffic light
     */
    @GetMapping(path = "/checkRadius")
    public Long checkRadius(@RequestParam Double latitude, @RequestParam Double longitude) {
        return actorRegistryDAO.findIfInRadius(latitude, longitude);
    }

    /**
     * Inserts new traffic light in db
     *
     * @param id        of a traffic light that is going to be stored in db
     * @param longitude of a traffic light that is going to be stored in db
     * @param latitude  of a traffic light that is going to be stored in db
     */
    public void addTrafficLight(Long id, Double longitude, Double latitude) {
        LOG.info("Inserting new Traffic Light with ID: " + id);
        TrafficLight trafficLight = new TrafficLight(longitude, latitude, id);
        actorRegistryDAO.addTrafficLight(trafficLight);
    }

    /**
     * Gets all traffic lights from db
     *
     * @return A list off all traffic lights
     */
    public List<TrafficLight> getAllTrafficLights() {
        LOG.info("Getting all traffic lights");
        return actorRegistryDAO.getAllTrafficLights();
    }
}
