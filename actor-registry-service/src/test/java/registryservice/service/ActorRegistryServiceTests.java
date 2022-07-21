package registryservice.service;

import org.mockito.*;
import registryservice.dao.ActorRegistryDAO;
import registryservice.dto.TrafficLight;
import registryservice.dto.Vehicle;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class ActorRegistryServiceTests {
    @Mock
    private ActorRegistryDAO actorRegistryDAO;

    @InjectMocks
    private static final ActorRegistryService actorRegistryService = new ActorRegistryService();

    private static final List<Vehicle> vehicleList = new ArrayList<>();
    private static final List<TrafficLight> trafficLightList = new ArrayList<>();

    @BeforeClass
    public static void setup() {
        Vehicle vehicle1 = new Vehicle("test1", "C4", "Citroen");
        vehicleList.add(vehicle1);
        TrafficLight trafficLight1 = new TrafficLight(48.5266, 18.5551, 1L);
        trafficLightList.add(trafficLight1);
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void addVehicleTest() {
        actorRegistryService.addVehicle("test2", "Q5", "Audi");
        Mockito.verify(actorRegistryDAO, Mockito.times(1)).addVehicle(Mockito.any());
    }

    @Test
    public void getAllVehiclesTest() {
        Mockito.when(actorRegistryDAO.getAllVehicles()).thenReturn(vehicleList);
        List<Vehicle> vehicles = actorRegistryService.getAllVehicles();
        Assert.assertNotNull(vehicles);
        Assert.assertEquals(1, vehicles.size());
        Assert.assertTrue(vehicles.iterator().hasNext());
        Assert.assertNotNull(vehicles.iterator().next());
        Assert.assertEquals("test1", vehicles.iterator().next().getVin());
        Assert.assertEquals("Citroen", vehicles.iterator().next().getProducer());
        Assert.assertEquals("C4", vehicles.iterator().next().getModel());
    }

    @Test
    public void addTrafficLight() {
        actorRegistryService.addTrafficLight(2L, 47.5481, 16.2568);
        Mockito.verify(actorRegistryDAO, Mockito.times(1)).addTrafficLight(Mockito.any());
    }

    @Test
    public void getAllTrafficLightsTest() {
        Mockito.when(actorRegistryDAO.getAllTrafficLights()).thenReturn(trafficLightList);
        List<TrafficLight> trafficLights = actorRegistryService.getAllTrafficLights();
        Assert.assertNotNull(trafficLights);
        Assert.assertEquals(1, trafficLights.size());
        Assert.assertTrue(trafficLights.iterator().hasNext());
        Assert.assertNotNull(trafficLights.iterator().next());
        Assert.assertEquals(Long.valueOf(1L), trafficLights.iterator().next().getId());
        Assert.assertEquals(Double.valueOf(48.5266), trafficLights.iterator().next().getLongitude());
        Assert.assertEquals(Double.valueOf(18.5551), trafficLights.iterator().next().getLatitude());
    }

    @Test
    public void checkRadiusTest() {
        Mockito.when(actorRegistryDAO.findIfInRadius(18.5549, 48.5265 )).thenReturn(1L);
        Long id = actorRegistryService.checkRadius(18.5549, 48.5265);
        Assert.assertEquals(Long.valueOf(1L), id);
    }

}

