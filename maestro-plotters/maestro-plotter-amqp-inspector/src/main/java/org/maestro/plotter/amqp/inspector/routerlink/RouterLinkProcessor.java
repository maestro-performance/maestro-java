/*
 * Copyright 2018 Otavio Rodolfo Piske
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.maestro.plotter.amqp.inspector.routerlink;

import org.maestro.plotter.common.RecordProcessor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A class for read data processor
 */
public class RouterLinkProcessor implements RecordProcessor {
    private final RouterLinkDataSet routerLinkDataSet = new RouterLinkDataSet();

    public RouterLinkDataSet getRouterLinkDataSet() {
        return routerLinkDataSet;
    }

    // Timestamp,Name,LinkDir,OperStatus, Identity,DeliveryCount,UndeliveredCount,PresettledCount,
    // UnsettledCount,ReleasedCount,ModifiedCount,AcceptedCount,RejectedCount,Capacity

    /**
     * Method for process read data from csv
     * @param records records from csv
     * @throws Exception implementation specific
     */
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
        routerLinkRecord.setReleasedCount(Long.parseLong(records[9]));
        routerLinkRecord.setModifiedCount(Long.parseLong(records[10]));
        routerLinkRecord.setAcceptedCount(Long.parseLong(records[11]));
        routerLinkRecord.setRejectedCount(Long.parseLong(records[12]));
        routerLinkRecord.setCapacity(Long.parseLong(records[13]));


        routerLinkDataSet.add(routerLinkRecord);
    }
}
