package net.orpiske.mpt.maestro.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.orpiske.mpt.common.exceptions.MaestroConnectionException;
import net.orpiske.mpt.maestro.exceptions.MalformedNoteException;
import net.orpiske.mpt.maestro.notes.MaestroNote;

// TODO: configure LWT
public abstract class AbstractMaestroPeer implements MqttCallback {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMaestroPeer.class);

    private MqttClient mqttClient;
    protected String clientName;
    protected String id;

    public AbstractMaestroPeer(final String url, final String clientName) throws MaestroConnectionException {

        // The client uses the mqtt://<host> url format so it's the same as the C client
        String adjustedUrl = StringUtils.replace(url, "mqtt", "tcp");

        UUID uuid = UUID.randomUUID();
        String clientId = uuid.toString();
        MemoryPersistence memoryPersistence = new MemoryPersistence();

        this.id = clientId;
        this.clientName = clientName;

        try {
            mqttClient = new MqttClient(adjustedUrl, clientName + "." + clientId, memoryPersistence);
            mqttClient.setCallback(this);
        }
        catch (MqttException e) {
            throw new MaestroConnectionException("Unable create a MQTT client instance : " + e.getMessage(),
                    e);
        }
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void connectionLost(Throwable throwable) {
        logger.warn("Connection lost");
    }

    public boolean isConnected() {
        return mqttClient.isConnected();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    public void connect() throws MaestroConnectionException {
        logger.debug("Connecting to maestro");
        MqttConnectOptions connOpts = new MqttConnectOptions();

        connOpts.setCleanSession(true);
        connOpts.setKeepAliveInterval(15000);

        try {
            mqttClient.connect();
        }
        catch (MqttException e) {
            throw new MaestroConnectionException("Unable to establish a connection to Maestro: " + e.getMessage(), e);
        }
    }

    public void disconnect() throws MaestroConnectionException {
        logger.debug("Disconnecting from maestro");

        try {
            mqttClient.disconnect();
        }
        catch (MqttException e) {
            throw new MaestroConnectionException("Unable to disconnect cleanly from Maestro: " + e.getMessage(), e);
        }
    }

    public void subscribe(final String[] topics) throws MaestroConnectionException {
        logger.debug("Subscribing to maestro topics {}", Arrays.toString(topics));

        int qos[] = new int[topics.length];

        for (int i = 0; i < topics.length; i++) {
            qos[i] = 0;
        }

        try {
            mqttClient.subscribe(topics, qos);
        }
        catch (MqttException e) {
            throw new MaestroConnectionException("Unable to subscribe to Maestro topics: " + e.getMessage(), e);
        }
    }

    public void messageArrived(String s, MqttMessage mqttMessage) {
        logger.trace("Message arrived on topic {}", s);

        byte[] payload = mqttMessage.getPayload();

        try {
            MaestroNote note = MaestroDeserializer.deserialize(payload);
            logger.trace("Message type: " + note.getClass());

            noteArrived(note);
        } catch (MalformedNoteException e) {
            logger.error("Invalid message type: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("I/O error: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unhandled exception: " + e.getMessage(), e);
        }
    }


    /**
     * Publish a maestro message
     * @param topic the topic to publish the message to
     * @param note the note to publish
     * @throws IOException in case of I/O errors
     * @throws MaestroConnectionException in case it fails to publish the message to the broker
     */
    protected void publish(final String topic, final MaestroNote note) throws IOException, MaestroConnectionException {
        try {
            mqttClient.publish(topic, note.serialize(), 0, false);
        } catch (MqttException e) {
            throw new MaestroConnectionException("Unable to publish a maestro message", e);
        }
    }

    /**
     * The entry point for handling Maestro messages
     * @param note
     */
    protected abstract void noteArrived(MaestroNote note) throws IOException, MaestroConnectionException;


}
