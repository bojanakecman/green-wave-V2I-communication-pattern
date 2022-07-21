package at.tuwien.dse.statustrackingservice.dao;

import at.tuwien.dse.statustrackingservice.connection.Connection;
import at.tuwien.dse.statustrackingservice.dto.Movement;
import at.tuwien.dse.statustrackingservice.dto.TrafficLightStatus;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * StatusTracking Repository that manages modifications to the MongoDB
 */
@Repository
public class StatusTrackingDAO {

    public static final Logger LOG = LoggerFactory.getLogger(StatusTrackingDAO.class);

    private static final String MOVEMENT_COLLECTION = "movements";

    private static final String STATUS_COLLECCTION = "statuses";

    /**
     * movements - MongoDB Collection used for storing Vehicles
     */
    private MongoCollection<Movement> movements;
    /**
     * trafficLights - MongoDB Collection used for storing traffic lights
     */
    private MongoCollection<TrafficLightStatus> trafficLightStatuses;

    @Autowired
    public StatusTrackingDAO(){
        try {
            movements = Connection.getDatabase().getCollection(MOVEMENT_COLLECTION, Movement.class);
            trafficLightStatuses = Connection.getDatabase().getCollection(STATUS_COLLECCTION, TrafficLightStatus.class);
        } catch (IOException e) {
            LOG.error("Error while connecting to MongoDB.");
        }
    }

    public StatusTrackingDAO(MongoCollection<Movement> movements, MongoCollection<TrafficLightStatus> trafficLightStatuses) {
        this.movements = movements;
        this.trafficLightStatuses = trafficLightStatuses;
    }


    /**
     * Inserts new movement to MongoDB collection
     *
     * @param movement to be inserted
     */
    public void addMovement(Movement movement) {
        try {
            movements.insertOne(movement);
        } catch (MongoWriteException e) {
            LOG.error("Error while writing in Mongo");
        }

    }

    /**
     * Inserts new traffic light status to MongoDB collection
     *
     * @param status to be inserted
     */
    public void addTrafficLightStatus(TrafficLightStatus status) {
        try {
            trafficLightStatuses.insertOne(status);
        } catch (MongoWriteException e) {
            LOG.error("Error while writing in Mongo");
        }

    }
}
