package registryservice.connection;


import com.mongodb.MongoClient;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class manages connection with MongoDB
 */
public class Connection {

    private static final Logger LOG = LoggerFactory.getLogger(Connection.class);

    private static MongoClient mongoClient;

    /**
     * Returns an instance of MongoDB if it is already opened, if not the new instance will be created and returned.
     * @return Instance of the MongoDB
     * @throws IOException if connection with database is not established successfully
     */
    public static MongoDatabase getDatabase() throws IOException {
        LOG.info("Retrieving instance of mongodb.");
        CodecRegistry pojoCodecRegistry = org.bson.codecs.configuration.CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), org.bson.codecs.configuration.CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        if (mongoClient == null) {
            String mongoDBURI = loadProperties().getProperty("spring.data.mongodb.uri");
            mongoClient = new MongoClient(new MongoClientURI(mongoDBURI));
        }
        return mongoClient.getDatabase("actors").withCodecRegistry(pojoCodecRegistry);
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
}
