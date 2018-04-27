package org.maestro.plotter.amqp.inspector.generalinfo;

import org.maestro.plotter.common.RecordProcessor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A class for read data processor
 */
public class GeneralInfoProcessor implements RecordProcessor {
    private final GeneralInfoDataSet generalInfoDataSet = new GeneralInfoDataSet();

    public GeneralInfoDataSet getGeneralInfoDataSet() {
        return generalInfoDataSet;
    }

    // Timestamp,Name,Version,Mode,LinkRoutes,AutoLinks,Links,Nodes,Addresses,Connections,PressettledCOunt,
    // DroppedPresettledCount,AcceptedCount,RejectedCount,ReleasedCount,ModifiedCount,IngressCount,EngressCount,
    // TransitCount,DelFromRouterContainer,DelToRouterContainer

    /**
     * Method for process read data from csv
     * @param records records from csv
     * @throws Exception implementation specific
     */
    @Override
    public void process(String... records) throws Exception {
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date timeStamp = formatter.parse(records[0]);
        GeneralInfoRecord generalInfoRecord = new GeneralInfoRecord();
        generalInfoRecord.setTimestamp(timeStamp.toInstant());

        generalInfoRecord.setName(records[1]);
        generalInfoRecord.setVersion(records[2]);
        generalInfoRecord.setMode(records[3]);
        generalInfoRecord.setLinkRoutersCount(Long.parseLong(records[4]));
        generalInfoRecord.setAutoLinksCount(Long.parseLong(records[5]));
        generalInfoRecord.setLinksCount(Long.parseLong(records[6]));
        generalInfoRecord.setNodesCount(Long.parseLong(records[7]));
        generalInfoRecord.setAddressCount(Long.parseLong(records[8]));
        generalInfoRecord.setConnetionsCount(Long.parseLong(records[9]));
        generalInfoRecord.setPresettledCount(Long.parseLong(records[10]));
        generalInfoRecord.setDroppedPresettledCount(Long.parseLong(records[11]));
        generalInfoRecord.setAcceptedCount(Long.parseLong(records[12]));
        generalInfoRecord.setRejectedCount(Long.parseLong(records[13]));
        generalInfoRecord.setReleasedCount(Long.parseLong(records[14]));
        generalInfoRecord.setModifiedCount(Long.parseLong(records[15]));
        generalInfoRecord.setIngressCount(Long.parseLong(records[16]));
        generalInfoRecord.setEngressCount(Long.parseLong(records[17]));
        generalInfoRecord.setTransitCount(Long.parseLong(records[18]));
        generalInfoRecord.setDeliveryFromRouterCount(Long.parseLong(records[19]));
        generalInfoRecord.setDeliveryToRouterCount(Long.parseLong(records[20]));


        generalInfoDataSet.add(generalInfoRecord);
    }
}
