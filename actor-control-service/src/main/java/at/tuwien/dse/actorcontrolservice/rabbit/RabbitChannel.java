package at.tuwien.dse.actorcontrolservice.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * In charge of establishing connection to RabbitMQ and rabbit channels
 */
public class RabbitChannel {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitChannel.class);
    private static final String ACTOR_QUEUE = "actor_queue";
    private static final String MOVEMENT_STATUS_EXCHANGE = "movement_status";
    private Channel channel;


    public RabbitChannel() {
        createConnection();
    }

    /**
     * Establishes connection to RabbitMQ
     *
     * @throws IOException if could not create connection
     */
    private void createConnection() {
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
            channel.exchangeDeclare(MOVEMENT_STATUS_EXCHANGE, "fanout");
            String mov_status_queue = channel.queueDeclare(ACTOR_QUEUE, false, false, false, null).getQueue();
            channel.queueBind(mov_status_queue, MOVEMENT_STATUS_EXCHANGE, "");
            LOG.info("Creating new rabbitmq channel.");
        } catch (IOException | TimeoutException e) {
            LOG.error(e.getMessage());
        }
    }

    public Channel getChannel() {
        return channel;
    }
}

