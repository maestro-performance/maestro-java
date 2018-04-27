package org.maestro.plotter.amqp.inspector.connections;

import org.maestro.plotter.amqp.inspector.memory.QDMemoryDataSet;
import org.maestro.plotter.amqp.inspector.memory.QDMemoryRecord;
import org.maestro.plotter.common.RecordProcessor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A class for read data processor
 */
public class ConnectionsProcessor implements RecordProcessor {
    private final ConnectionsDataSet connectionsDataSet = new ConnectionsDataSet();

    public ConnectionsDataSet getConnectionsDataSet() {
        return connectionsDataSet;
    }

//    Timestamp,Name,Host,Role,Dir,Opened,Identity,User,sasl,Encrypted,sslProto,sslCipher,Tenant,Authenticated,Properties

    /**
     * Method for process read data from csv
     * @param records records from csv
     * @throws Exception implementation specific
     */
    @Override
    public void process(String... records) throws Exception {
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date timeStamp = formatter.parse(records[0]);
        ConnectionsRecord connectionsRecord = new ConnectionsRecord();
        connectionsRecord.setTimestamp(timeStamp.toInstant());

        connectionsRecord.setName(records[1]);

        connectionsRecord.setHost(records[2]);
        connectionsRecord.setRole(records[3]);
        connectionsRecord.setDir(records[4]);
        connectionsRecord.setOpened(records[5]);
        connectionsRecord.setIdentity(records[6]);
        connectionsRecord.setUser(records[7]);
        connectionsRecord.setSasl(records[8]);
        connectionsRecord.setEncrypted(records[9]);
        connectionsRecord.setSslProto(records[10]);
        connectionsRecord.setSslCipher(records[11]);
        connectionsRecord.setTenant(records[12]);
        connectionsRecord.setAuthenticated(records[13]);
        connectionsRecord.setProperties(records[14]);

        connectionsDataSet.add(connectionsRecord);
    }
}
