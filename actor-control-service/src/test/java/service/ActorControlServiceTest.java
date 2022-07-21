package service;

import at.tuwien.dse.actorcontrolservice.dao.ActorControlDAO;
import at.tuwien.dse.actorcontrolservice.dto.Movement;
import at.tuwien.dse.actorcontrolservice.dto.TrafficLight;
import at.tuwien.dse.actorcontrolservice.dto.TrafficLightStatus;
import at.tuwien.dse.actorcontrolservice.rabbit.RabbitChannel;
import at.tuwien.dse.actorcontrolservice.service.ActorControlService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;


public class ActorControlServiceTest {

    @Mock
    private Client client;
    @Mock
    private ConcurrentHashMap<Long, TrafficLightStatus> statusMap;
    @Mock
    private Map<Long, TrafficLight> trafficLights;
    @Mock
    RabbitChannel rabbitChannel;
    @Mock
    private ActorControlDAO actorControlDAO;
    @Mock
    private Channel channel;

    private static ActorControlService actorControlService;
    private static Movement movement = new Movement();



    @Before
    public void init() throws IOException{
        client = Mockito.mock(Client.class);
        statusMap = Mockito.mock(ConcurrentHashMap.class);
        trafficLights = Mockito.mock(Map.class);
        rabbitChannel = Mockito.mock(RabbitChannel.class);
        actorControlDAO = Mockito.mock(ActorControlDAO.class);
        actorControlService = new ActorControlService(client, statusMap, trafficLights, actorControlDAO);

        TrafficLightStatus status = new TrafficLightStatus();
        status.setTrafficLightId(1L);
        status.setGreen(false);
        status.setDateTime(LocalDateTime.now());

        movement.setVin("test");
        movement.setSpeed(60.0);
        movement.setLatitude(48.5233);
        movement.setLongitude(16.0512);
        movement.setCrash(false);
        movement.setDateTime(LocalDateTime.now());

        channel = Mockito.mock(Channel.class);

        TrafficLight trafficLight = new TrafficLight(1L, 16.0525, 48.5255);

        Mockito.when(statusMap.get(Mockito.anyLong()))
                .thenReturn(status);
        Mockito.when(trafficLights.get(Mockito.anyLong()))
                .thenReturn(trafficLight);
        Mockito.when(rabbitChannel.getChannel())
                .thenReturn(channel);
        doNothing().when(channel).basicPublish(eq(""),eq("speed_queue"), eq(null), Mockito.any());
        doNothing().when(actorControlDAO).addMovement(Mockito.any());

    }

    @Test
    public void calculateDistanceInMeterTest(){
        int distance = actorControlService.calculateDistanceInMeter(48.15254, 16.33825, 48.1756, 16.3374);
        Assert.assertEquals(2565, distance);
    }

    @Test
    public void determineSpeed() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        actorControlService.determineSpeed(movement, 1L, rabbitChannel);
        movement.setSpeed(94.68);
        String msg = objectMapper.writeValueAsString(movement);
        Mockito.verify(rabbitChannel, Mockito.times(1)).getChannel();
        Mockito.verify(channel, Mockito.times(1)).basicPublish("", "speed_queue", null, msg.getBytes());
        Mockito.verify(actorControlDAO, Mockito.times(1)).addMovement(Mockito.any());

    }
}
