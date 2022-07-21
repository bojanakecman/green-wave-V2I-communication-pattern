package registryservice.dao;

import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import registryservice.connection.Connection;
import registryservice.dto.TrafficLight;
import registryservice.dto.Vehicle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;

/**
 * ActorRegistry Repository that manages modifications to the MongoDB
 */
@Repository
public class ActorRegistryDAO {


    private static final Logger LOG = LoggerFactory.getLogger(ActorRegistryDAO.class);

    private static final String VEHICLE_COLLECTION = "vehicles";
    private static final String TRAFFIC_LIGHTS_COLLECTION = "traffic_lights";
    private static final String ID = "id";
    private static final String COORDINATES = "coordinates";
    private static final String LOCATION = "location";

    /**
     * vehicles - MongoDB Collection used for storing Vehicles
     */
    private MongoCollection<Vehicle> vehicles;
    /**
     * trafficLights - MongoDB Collection used for storing traffic lights
     */
    private MongoCollection<Document> trafficLights;

    @Autowired
    public ActorRegistryDAO() {
        try {
            Connection.getDatabase().drop();
            Connection.getDatabase().getCollection(TRAFFIC_LIGHTS_COLLECTION).dropIndexes();
            vehicles = Connection.getDatabase().getCollection(VEHICLE_COLLECTION, Vehicle.class);
            vehicles.createIndex(Indexes.text("vin"), new IndexOptions().unique(true));
            trafficLights = Connection.getDatabase().getCollection(TRAFFIC_LIGHTS_COLLECTION);
            //trafficLights.createIndex(Indexes.text("id"), new IndexOptions().unique(true));
            trafficLights.createIndex(Indexes.geo2dsphere(LOCATION));
        } catch (IOException e) {
            LOG.error("Error while connecting to MongoDB.");
        }
    }

    public ActorRegistryDAO(MongoCollection<Vehicle> vehicles, MongoCollection<Document> trafficLights) {
        this.vehicles = vehicles;
        this.trafficLights = trafficLights;
    }

    /**
     * Inserts new vehicle in db
     *
     * @param vehicle to be stored in db
     */
    public void addVehicle(Vehicle vehicle) {
        try {
            LOG.info("Inserting new vehicle: " + vehicle.getVin());
            vehicles.insertOne(vehicle);
        } catch (MongoWriteException e) {
            LOG.error("Error while writing in Mongo");
        }
    }

    /**
     * Gets all vehicles from db
     *
     * @return A list of all vehicles
     */
    public List<Vehicle> getAllVehicles() {
        LOG.info("Getting all vehicles");
        FindIterable<Vehicle> iterable =  vehicles.find();
        ArrayList<Vehicle> vehicles = new ArrayList<>();
        for (Vehicle v : iterable) {
            vehicles.add(v);
        }
        return vehicles;
    }

    /**
     * Inserts new traffic light to db
     *
     * @param trafficLight to be stored in db
     */
    public void addTrafficLight(TrafficLight trafficLight) {
        LOG.info("Inserting new traffic light");
        List<Double> coordinates = new ArrayList<>();
        coordinates.add(trafficLight.getLatitude());
        coordinates.add(trafficLight.getLongitude());
        Document location = new Document("type", "Point").append(COORDINATES, coordinates);
        String _id = new ObjectId().toHexString();
        Document document = new Document("_id", _id)
                .append(ID, trafficLight.getId())
                .append(LOCATION, location);
        trafficLights.insertOne(document);
    }

    /**
     * Checks if the vehicle is in radius of some traffic light
     *
     * @param latitude   of checked vehicle
     * @param longitude  of checked vehicle
     * @return An id of the found traffic light
     */
    public Long findIfInRadius(double latitude, double longitude) {
        Position position = new Position(latitude, longitude);
        Point point = new Point(position);
        MongoCursor<Document> trafficLight = trafficLights.find(and(eq(ID, 3L), near(LOCATION, point, 1300.0, 0.0))).iterator();

        if (!trafficLight.hasNext()) {
            trafficLight = trafficLights.find(and(eq(ID, 2L), near(LOCATION, point, 700.0, 0.0))).iterator();

            if (!trafficLight.hasNext()) {
                trafficLight = trafficLights.find(and(eq(ID, 1L), near(LOCATION, point, 1000.0, 0.0))).iterator();
            }
        }
        return trafficLight.hasNext() ? (Long) trafficLight.next().get(ID) : 0L;

    }

    /**
     * Gets all traffic lights
     *
     * @return A list of all traffic lights
     */
    public List<TrafficLight> getAllTrafficLights() {
        LOG.info("Getting all traffic lights");
        FindIterable<Document> iterable = trafficLights.find();
        ArrayList<TrafficLight> trafficLights = new ArrayList<>();
        for (Document d : iterable) {
            trafficLights.add(mapDocumentToTrafficLight(d));
        }
        return trafficLights;
    }

    /**
     * Maps Mongo document to TrafficLightDTO object
     *
     * @param d Mongo document to be mapped
     * @return TrafficLightDTO object
     */
    private TrafficLight mapDocumentToTrafficLight(Document d) {
        List<?> coordinates = (List<?>) ((Document) d.get(LOCATION)).get(COORDINATES);
        Double latitude = ((Double) coordinates.get(0));
        Double longitude = ((Double) coordinates.get(1));
        return new TrafficLight(longitude, latitude, d.getLong(ID));
    }

}
