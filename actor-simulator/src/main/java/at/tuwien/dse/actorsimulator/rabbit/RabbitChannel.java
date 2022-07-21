package at.tuwien.dse.actorsimulator.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

/**
 * In charge of establishing connection to RabbitMQ and rabbit channels
 */
public class RabbitChannel {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitChannel.class);
    private static final String MOVEMENT_QUEUE = "movement_queue";
    private static final String SPEED_QUEUE = "speed_queue";
    private Channel channel;


    public RabbitChannel() throws IOException {
        createConnection();
    }

    /**
     * Establishes connection to RabbitMQ
     *
     * @throws IOException if could not create connection
     */
    private void createConnection() throws IOException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("rabbitmq");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");

        LOG.info("Opening connection with rabbitmq.");
        Connection connection = null;
        try {
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(MOVEMENT_QUEUE, false, false, false, null);
            channel.queueDeclare(SPEED_QUEUE, false, false, false, null);

            LOG.info("Creating new rabbitmq channel.");
        } catch (IOException | TimeoutException e) {
            LOG.error(e.getMessage());
        }
    }

    /**
     * Method used to load host and port for MongoDB from application.properties
     *
     * @return properties from application.properties file
     * @throws IOException if could not load properties
     */
    private static Properties loadProperties() throws IOException {
        LOG.info("Loading host and port for mongodb from application.properties");
        Properties properties = new Properties();
        try (InputStream in = Connection.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(in);
        }
        return properties;
    }

    public Channel getChannel() {
        return channel;
    }
}

