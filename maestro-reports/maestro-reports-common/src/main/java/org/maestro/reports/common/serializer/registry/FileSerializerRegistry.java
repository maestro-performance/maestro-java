/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
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

package org.maestro.reports.common.serializer.registry;

import org.maestro.plotter.amqp.inspector.serializer.ConnectionsDataSerializer;
import org.maestro.plotter.amqp.inspector.serializer.QDMemoryDataSetSerializer;
import org.maestro.plotter.amqp.inspector.serializer.QDRuntimeInfoDataSetSerializer;
import org.maestro.plotter.amqp.inspector.serializer.RouterLinkDataSetSerializer;
import org.maestro.plotter.common.serializer.MaestroSerializer;
import org.maestro.plotter.inspector.serializer.HeapMemoryDataSerializer;
import org.maestro.plotter.inspector.serializer.MemoryAreasDataSerializer;
import org.maestro.plotter.inspector.serializer.QueueDataSerializer;
import org.maestro.plotter.latency.serializer.DefaultLatencySerializer;
import org.maestro.plotter.rate.serializer.RateSerializer;

import java.util.HashMap;
import java.util.Map;

public class FileSerializerRegistry {
    private final Map<String, MaestroSerializer> registry = new HashMap<>();
    private static FileSerializerRegistry instance;

    private FileSerializerRegistry() {
        registry.put("sender.dat", new RateSerializer());
        registry.put("receiver.dat", new RateSerializer());
        registry.put("receiverd-latency.hdr", new DefaultLatencySerializer());
        registry.put("heap.csv", new HeapMemoryDataSerializer());
        registry.put("memory-areas.csv", new MemoryAreasDataSerializer());
        registry.put("queues.csv", new QueueDataSerializer());
        registry.put("general.csv", new QDRuntimeInfoDataSetSerializer());
        registry.put("qdmemory.csv", new QDMemoryDataSetSerializer());
        registry.put("routerLink.csv", new RouterLinkDataSetSerializer());
        registry.put("connections.csv", new ConnectionsDataSerializer());
    }


    public MaestroSerializer getSerializer(final String fileName) {
        return registry.get(fileName);
    }

    public static FileSerializerRegistry getInstance() {
        if (instance == null) {
            synchronized (FileSerializerRegistry.class) {
                if (instance == null) {
                     instance = new FileSerializerRegistry();
                }
            }
        }

        return instance;
    }
}
