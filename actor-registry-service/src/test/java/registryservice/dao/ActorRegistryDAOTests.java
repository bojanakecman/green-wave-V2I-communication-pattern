package registryservice.dao;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import registryservice.dto.TrafficLight;
import registryservice.dto.Vehicle;
import java.util.ArrayList;
import java.util.List;

public class ActorRegistryDAOTests {
    private static final String ID = "id";
    private static final String LOCATION = "location";
    private static final String COORDINATES = "coordinates";

    @Mock
    private static MongoCollection vehicles;
    @Mock
    private static MongoCollection trafficLights;

    private static ActorRegistryDAO actorRegistryDAO;
    private static Vehicle vehicle = new Vehicle();
    private static TrafficLight trafficLight = new TrafficLight();

    @BeforeClass
    public static void setup() {
        vehicle = new Vehicle("test1", "C4", "Citroen");
        trafficLight = new TrafficLight(48.5266, 18.5551, 1L);
    }

    @Before
    public void init() {
        vehicles = Mockito.mock(MongoCollection.class);
        trafficLights = Mockito.mock(MongoCollection.class);
        actorRegistryDAO = new ActorRegistryDAO(vehicles, trafficLights);
        // Creating vehicle and traffic light document
        Vehicle vehicle = new Vehicle("test", "Citroen", "C4");
        List<Double> coordinates = new ArrayList<>();
        coordinates.add(48.1555);
        coordinates.add(16.2052);
        Document position = new Document("type", "Point").append(COORDINATES, coordinates);
        Document trafficLightDocument = new Document("_id", "test")
                .append(ID, 2L)
                .append(LOCATION, position);

        // Mocking vehicles collection
        FindIterable vehicleDocuments = Mockito.mock(FindIterable.class);
        MongoCursor vehicleCursor = Mockito.mock(MongoCursor.class);

        Mockito.when(vehicles.find())
                .thenReturn(vehicleDocuments);
        Mockito.when(vehicleDocuments.iterator())
                .thenReturn(vehicleCursor);
        Mockito.when(vehicleCursor.hasNext())
                .thenReturn(true)
                .thenReturn(false);
        Mockito.when(vehicleCursor.next())
                .thenReturn(vehicle);

        // Mocking traffic lights collection
        FindIterable trafficLightDocuments = Mockito.mock(FindIterable.class);
        MongoCursor trafficLightCursor = Mockito.mock(MongoCursor.class);

        Mockito.when(trafficLights.find())
                .thenReturn(trafficLightDocuments);
        Mockito.when(trafficLights.find(Mockito.any(Bson.class)))
                .thenReturn(trafficLightDocuments);
        Mockito.when(trafficLightDocuments.iterator())
                .thenReturn(trafficLightCursor);
        Mockito.when(trafficLightCursor.hasNext())
                .thenReturn(true)
                .thenReturn(false);
        Mockito.when(trafficLightCursor.next())
                .thenReturn(trafficLightDocument);
    }

    @Test
    public void addVehicleTest() {
        actorRegistryDAO.addVehicle(vehicle);
        Mockito.verify(vehicles, Mockito.times(1)).insertOne(Mockito.any(Vehicle.class));
    }

    @Test
    public void addTrafficLightTest() {
        actorRegistryDAO.addTrafficLight(trafficLight);
        Mockito.verify(trafficLights, Mockito.times(1)).insertOne(Mockito.any(Document.class));
    }

    @Test
    public void getAllVehiclesTest() {
        List<Vehicle> vehicles = actorRegistryDAO.getAllVehicles();
        Assert.assertNotNull(vehicles);
        Assert.assertEquals(1, vehicles.size());
        Assert.assertTrue(vehicles.iterator().hasNext());
        Assert.assertNotNull(vehicles.iterator().next());
        Assert.assertEquals("test", vehicles.iterator().next().getVin());
    }

    @Test
    public void getAllTrafficLightsTest() {
        List<TrafficLight> trafficLights = actorRegistryDAO.getAllTrafficLights();
        Assert.assertNotNull(trafficLights);
        Assert.assertEquals(1, trafficLights.size());
        Assert.assertTrue(trafficLights.iterator().hasNext());
        Assert.assertNotNull(trafficLights.iterator().next());
        Assert.assertEquals(Long.valueOf(2L), trafficLights.iterator().next().getId());
    }
}
