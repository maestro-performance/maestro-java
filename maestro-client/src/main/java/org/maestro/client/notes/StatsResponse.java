/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.maestro.client.notes;

import org.maestro.common.client.notes.MaestroCommand;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

public class StatsResponse extends MaestroResponse {
    private int childCount;
    private String roleInfo;
    private short statsType;

    private String timestamp;
    private long count;
    private double rate;
    private double latency;

    public StatsResponse() {
        super(MaestroCommand.MAESTRO_NOTE_STATS);
    }

    public StatsResponse(final MessageUnpacker unpacker) throws IOException {
        super(MaestroCommand.MAESTRO_NOTE_STATS, unpacker);

        childCount = unpacker.unpackInt();
        roleInfo = unpacker.unpackString();
        statsType = unpacker.unpackShort();

        timestamp = unpacker.unpackString();
        count = unpacker.unpackLong();
        rate = unpacker.unpackDouble();
        latency = unpacker.unpackDouble();
    }

    @Override
    protected MessageBufferPacker pack() throws IOException {
        MessageBufferPacker packer = super.pack();

        packer.packLong(this.childCount);
        packer.packString(this.roleInfo);
        packer.packShort(this.statsType);
        packer.packString(this.timestamp);
        packer.packLong(this.count);
        packer.packDouble(this.rate);
        packer.packDouble(this.latency);

        return packer;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public long getCount() {
        return count;
    }

    public double getRate() {
        return rate;
    }

    public double getLatency() {
        return latency;
    }

    public int getChildCount() {
        return childCount;
    }


    public String getRoleInfo() {
        return roleInfo;
    }

    public short getStatsType() {
        return statsType;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public void setRoleInfo(String roleInfo) {
        this.roleInfo = roleInfo;
    }

    public void setStatsType(short statsType) {
        this.statsType = statsType;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public void setLatency(double latency) {
        this.latency = latency;
    }

    @Override
    public String toString() {
        return "StatsResponse{" +
                "childCount=" + childCount +
                ", roleInfo='" + roleInfo + '\'' +
                ", statsType=" + statsType +
                ", timestamp='" + timestamp + '\'' +
                ", count=" + count +
                ", rate=" + rate +
                ", latency=" + latency +
                "} " + super.toString();
    }
}
