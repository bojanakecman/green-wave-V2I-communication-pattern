package dao;

import at.tuwien.dse.statustrackingservice.dao.StatusTrackingDAO;
import at.tuwien.dse.statustrackingservice.dto.Movement;
import at.tuwien.dse.statustrackingservice.dto.TrafficLightStatus;
import com.mongodb.client.MongoCollection;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import java.time.LocalDateTime;


public class StatusTrackingDAOTest {
    @Mock
    private static MongoCollection movements;
    @Mock
    private static MongoCollection trafficLightStatuses;

    private static StatusTrackingDAO statusTrackingDAO;
    private static Movement movement = new Movement();
    private static TrafficLightStatus status = new TrafficLightStatus();

    @BeforeClass
    public static void setup() {
        movement = new Movement();
        movement.setVin("test");
        movement.setSpeed(55.0);
        movement.setCrash(false);
        movement.setLatitude(48.5846);
        movement.setLongitude(16.0258);
        movement.setDateTime(LocalDateTime.now());
        status = new TrafficLightStatus(true, 1L, LocalDateTime.now());
    }

    @Before
    public void init() {
        movements = Mockito.mock(MongoCollection.class);
        trafficLightStatuses = Mockito.mock(MongoCollection.class);
        statusTrackingDAO = new StatusTrackingDAO(movements, trafficLightStatuses);
    }

    @Test
    public void addMovementTest() {
        statusTrackingDAO.addMovement(movement);
        Mockito.verify(movements, Mockito.times(1)).insertOne(Mockito.any(Movement.class));
    }

    @Test
    public void addTrafficLightStatusTest() {
        statusTrackingDAO.addTrafficLightStatus(status);
        Mockito.verify(trafficLightStatuses, Mockito.times(1)).insertOne(Mockito.any(TrafficLightStatus.class));
    }

}
