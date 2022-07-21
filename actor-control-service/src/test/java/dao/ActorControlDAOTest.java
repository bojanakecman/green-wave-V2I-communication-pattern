package dao;

import at.tuwien.dse.actorcontrolservice.dao.ActorControlDAO;
import at.tuwien.dse.actorcontrolservice.dto.Movement;
import com.mongodb.client.MongoCollection;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;


public class ActorControlDAOTest {
    @Mock
    private static MongoCollection movements;

    private static ActorControlDAO actorControlDAO;
    private static Movement movement = new Movement();

    @BeforeClass
    public static void setup() {
        movement = new Movement();
        movement.setVin("test");
        movement.setSpeed(90.0);
        movement.setCrash(false);
        movement.setLongitude(48.5421);
        movement.setLatitude(18.6525);
    }

    @Before
    public void init() {
        movements = Mockito.mock(MongoCollection.class);
        actorControlDAO = new ActorControlDAO(movements);
    }

    @Test
    public void addMovementTest() {
        actorControlDAO.addMovement(movement);
        Mockito.verify(movements, Mockito.times(1)).insertOne(Mockito.any(Movement.class));
    }
}
