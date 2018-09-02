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


        generalInfoDataSet.add(generalInfoRecord);
    }
}
