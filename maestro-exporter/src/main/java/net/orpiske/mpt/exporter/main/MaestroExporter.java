package net.orpiske.mpt.exporter.main;

import net.orpiske.mpt.maestro.Maestro;
import net.orpiske.mpt.maestro.client.MaestroCollector;
import net.orpiske.mpt.maestro.notes.MaestroNote;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class MaestroExporter {
    private static final Logger logger = LoggerFactory.getLogger(MaestroExporter.class);

    private boolean running = true;
    private Maestro maestro = null;
    private MaestroCollector maestroCollector;

    public MaestroExporter(final String maestroUrl, final String exportUrl) throws MqttException {
        maestro = new Maestro(maestroUrl);
        maestroCollector = new MaestroCollector(maestroUrl);
    }

    public int run() throws MqttException, IOException {
        while (running) {
            logger.debug("Sending requests");
            maestro.statsRequest();

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            List<MaestroNote> notes = maestro.collect();

            for (MaestroNote note : notes) {
                System.out.println("Note = " + note.toString());
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return 0;
    }
}
