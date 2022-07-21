package service;

import at.tuwien.dse.apigateway.dto.TrafficLight;
import at.tuwien.dse.apigateway.dto.Vehicle;
import at.tuwien.dse.apigateway.service.ApiGatewayService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.List;

public class ApiGatewayServiceTests {

    @Mock
    private Client client;

    @Mock
    private static Response response;

    private static ApiGatewayService apiGatewayService;

    @BeforeClass
    public static void setup() {
    }

    @Before
    public void init() {
        client = Mockito.mock(Client.class);
        response = Mockito.mock(Response.class);
        apiGatewayService = new ApiGatewayService(client);

        // Mocking response for REST request
        WebTarget webTarget = Mockito.mock(WebTarget.class);
        Invocation.Builder builder = Mockito.mock(Invocation.Builder.class);
        Invocation invocation = Mockito.mock(Invocation.class);

        //String responseString = "{\"vin\":\"test\",\"model\":\"C4\",\"producer\":\"Citroen\"}";
        String responseString = "";
        Mockito.when(client.target(Mockito.anyString()))
                .thenReturn(webTarget);
        Mockito.when(webTarget.queryParam(Mockito.anyString(), Mockito.any()))
                .thenReturn(webTarget);
        Mockito.when(webTarget.request())
                .thenReturn(builder);
        Mockito.when(builder.get())
                .thenReturn(response);
        Mockito.when(builder.build(Mockito.anyString()))
                .thenReturn(invocation);
        Mockito.when(builder.build(Mockito.anyString(), Mockito.any()))
                .thenReturn(invocation);
        Mockito.when(invocation.property(Mockito.anyString(), Mockito.any()))
                .thenReturn(invocation);
        Mockito.when(invocation.invoke())
                .thenReturn(response);

        Mockito.when(response.getStatus())
                .thenReturn(HttpStatus.OK.value());
        Mockito.when(response.readEntity(String.class))
                .thenReturn(responseString);
    }

    @Test
    public void addVehicleTest() {
        ResponseEntity responseEntity = apiGatewayService.addVehicle("Citroen", "test1", "C4");
        Assert.assertNotNull(responseEntity);
        Assert.assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCodeValue());
        Assert.assertTrue(responseEntity.hasBody());
        Assert.assertNotNull(responseEntity.getBody());
        Assert.assertEquals("", responseEntity.getBody());
    }

    @Test
    public void addTrafficLightTest() {
        ResponseEntity responseEntity = apiGatewayService.addTrafficLight(49.5263, 20.8565, 5L);
        Assert.assertNotNull(responseEntity);
        Assert.assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCodeValue());
        Assert.assertTrue(responseEntity.hasBody());
        Assert.assertNotNull(responseEntity.getBody());
        Assert.assertEquals("", responseEntity.getBody());
    }

    @Test
    public void getAllVehiclesTest() {
        ResponseEntity<List<Vehicle>> responseEntity = apiGatewayService.getAllVehicles();
        Assert.assertNotNull(responseEntity);
        Assert.assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCodeValue());
        Assert.assertTrue(responseEntity.hasBody());
        Assert.assertNotNull(responseEntity.getBody());
        Assert.assertTrue(responseEntity.getBody().isEmpty());
    }

    @Test
    public void getAllTrafficLightsTest() {
        ResponseEntity<List<TrafficLight>> responseEntity = apiGatewayService.getAllTrafficLights();
        Assert.assertNotNull(responseEntity);
        Assert.assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCodeValue());
        Assert.assertTrue(responseEntity.hasBody());
        Assert.assertNotNull(responseEntity.getBody());
        Assert.assertTrue(responseEntity.getBody().isEmpty());
    }
}
