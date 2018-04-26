package org.maestro.plotter.amqp.inspector.routerlink;

import org.maestro.plotter.common.RecordProcessor;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RouterLinkProcessor implements RecordProcessor {
    private final RouterLinkDataSet routerLinkDataSet = new RouterLinkDataSet();

    public RouterLinkDataSet getRouterLinkDataSet() {
        return routerLinkDataSet;
    }

    // Timestamp,Name,LinkType,LinkDir,Identity,OperStatus,DeliveryCount,UndeliveredCount,PresettledCount,
    // UnsettledCount,DroppedPresettledCount,ReleasedCount,ModifiedCount,AcceptedCount,RejectedCount,Capacity
    @Override
    public void process(String... records) throws Exception {
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date timeStamp = formatter.parse(records[0]);
        RouterLinkRecord routerLinkRecord = new RouterLinkRecord();
        routerLinkRecord.setTimestamp(timeStamp.toInstant());

        routerLinkRecord.setName(records[1]);

        routerLinkRecord.setDeliveryCount(Long.parseLong(records[5]));
        routerLinkRecord.setUndeliveredCount(Long.parseLong(records[6]));
        routerLinkRecord.setPresettledCount(Long.parseLong(records[7]));
        routerLinkRecord.setUnsettledCount(Long.parseLong(records[8]));
        routerLinkRecord.setDroppedPresettledCount(Long.parseLong(records[9]));
        routerLinkRecord.setReleasedCount(Long.parseLong(records[10]));
        routerLinkRecord.setModifiedCount(Long.parseLong(records[11]));
        routerLinkRecord.setAcceptedCount(Long.parseLong(records[12]));
        routerLinkRecord.setRejectedCount(Long.parseLong(records[13]));
        routerLinkRecord.setCapacity(Long.parseLong(records[14]));


        routerLinkDataSet.add(routerLinkRecord);
    }
}
