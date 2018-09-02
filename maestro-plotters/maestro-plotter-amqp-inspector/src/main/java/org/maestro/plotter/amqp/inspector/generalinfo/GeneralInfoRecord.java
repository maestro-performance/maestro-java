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

import org.maestro.plotter.common.InstantRecord;

import java.time.Instant;
import java.util.Objects;

/**
 * A class represents single record from csv file
 */
public class GeneralInfoRecord implements Comparable<GeneralInfoRecord>, InstantRecord {
    private Instant timestamp;
    private String name;
    private String version;
    private String mode;
    private long linkRoutersCount;
    private long autoLinksCount;
    private long linksCount;
    private long nodesCount;
    private long addressCount;
    private long connetionsCount;
    private long presettledCount;
    private long droppedPresettledCount;
    private long acceptedCount;
    private long rejectedCount;
    private long releasedCount;
    private long modifiedCount;
    private long ingressCount;
    private long engressCount;
    private long transitCount;
    private long deliveryFromRouterCount;
    private long deliveryToRouterCount;


    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public long getLinkRoutersCount() {
        return linkRoutersCount;
    }

    public void setLinkRoutersCount(long linkRoutersCount) {
        this.linkRoutersCount = linkRoutersCount;
    }

    public long getAutoLinksCount() {
        return autoLinksCount;
    }

    public void setAutoLinksCount(long autoLinksCount) {
        this.autoLinksCount = autoLinksCount;
    }

    public long getLinksCount() {
        return linksCount;
    }

    public void setLinksCount(long linksCount) {
        this.linksCount = linksCount;
    }

    public long getNodesCount() {
        return nodesCount;
    }

    public void setNodesCount(long nodesCount) {
        this.nodesCount = nodesCount;
    }

    public long getAddressCount() {
        return addressCount;
    }

    public void setAddressCount(long addressCount) {
        this.addressCount = addressCount;
    }

    public long getConnetionsCount() {
        return connetionsCount;
    }

    public void setConnetionsCount(long connetionsCount) {
        this.connetionsCount = connetionsCount;
    }

    public long getPresettledCount() {
        return presettledCount;
    }

    public void setPresettledCount(long presettledCount) {
        this.presettledCount = presettledCount;
    }

    public long getDroppedPresettledCount() {
        return droppedPresettledCount;
    }

    public void setDroppedPresettledCount(long droppedPresettledCount) {
        this.droppedPresettledCount = droppedPresettledCount;
    }

    public long getAcceptedCount() {
        return acceptedCount;
    }

    public void setAcceptedCount(long acceptedCount) {
        this.acceptedCount = acceptedCount;
    }

    public long getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(long rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public long getModifiedCount() {
        return modifiedCount;
    }

    public void setModifiedCount(long modifiedCount) {
        this.modifiedCount = modifiedCount;
    }

    public long getIngressCount() {
        return ingressCount;
    }

    public void setIngressCount(long ingressCount) {
        this.ingressCount = ingressCount;
    }

    public long getEngressCount() {
        return engressCount;
    }

    public void setEngressCount(long engressCount) {
        this.engressCount = engressCount;
    }

    public long getTransitCount() {
        return transitCount;
    }

    public void setTransitCount(long transitCount) {
        this.transitCount = transitCount;
    }

    public long getDeliveryFromRouterCount() {
        return deliveryFromRouterCount;
    }

    public void setDeliveryFromRouterCount(long deliveryFromRouterCount) {
        this.deliveryFromRouterCount = deliveryFromRouterCount;
    }

    public long getDeliveryToRouterCount() {
        return deliveryToRouterCount;
    }

    public void setDeliveryToRouterCount(long deliveryToRouterCount) {
        this.deliveryToRouterCount = deliveryToRouterCount;
    }

    public long getReleasedCount() {
        return releasedCount;
    }

    public void setReleasedCount(long releasedCount) {
        this.releasedCount = releasedCount;
    }

    @Override
    public int compareTo(GeneralInfoRecord routerLinkRecord) {
        return this.getTimestamp().compareTo(routerLinkRecord.getTimestamp());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeneralInfoRecord that = (GeneralInfoRecord) o;
        return linkRoutersCount == that.linkRoutersCount &&
                autoLinksCount == that.autoLinksCount &&
                linksCount == that.linksCount &&
                nodesCount == that.nodesCount &&
                addressCount == that.addressCount &&
                connetionsCount == that.connetionsCount &&
                presettledCount == that.presettledCount &&
                droppedPresettledCount == that.droppedPresettledCount &&
                acceptedCount == that.acceptedCount &&
                rejectedCount == that.rejectedCount &&
                releasedCount == that.releasedCount &&
                modifiedCount == that.modifiedCount &&
                ingressCount == that.ingressCount &&
                engressCount == that.engressCount &&
                transitCount == that.transitCount &&
                deliveryFromRouterCount == that.deliveryFromRouterCount &&
                deliveryToRouterCount == that.deliveryToRouterCount &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(name, that.name) &&
                Objects.equals(version, that.version) &&
                Objects.equals(mode, that.mode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, name, version, mode, linkRoutersCount, autoLinksCount,
                linksCount, nodesCount, addressCount, connetionsCount, presettledCount, droppedPresettledCount,
                acceptedCount, rejectedCount, modifiedCount, ingressCount, engressCount, transitCount,
                deliveryFromRouterCount, deliveryToRouterCount);
    }

    @Override
    public String toString() {
        return "GeneralStatistics{" +
                "timestamp=" + timestamp +
                ", name='" + name + '\'' +
                ", version=" + version +
                ", mode=" + mode +
                ", linkRoutersCount=" + linkRoutersCount +
                ", autoLinksCount=" + autoLinksCount +
                ", linksCount=" + linksCount +
                ", nodesCount=" + nodesCount +
                ", addressCount=" + addressCount +
                ", connetionsCount=" + connetionsCount +
                ", presettledCount=" + presettledCount +
                ", droppedPresettledCount=" + droppedPresettledCount +
                ", acceptedCount=" + acceptedCount +
                ", rejectedCount=" + rejectedCount +
                ", modifiedCount=" + modifiedCount +
                ", ingressCount=" + ingressCount +
                ", engressCount=" + engressCount +
                ", transitCount=" + transitCount +
                ", deliveryFromRouterCount=" + deliveryFromRouterCount +
                ", deliveryToRouterCount=" + deliveryToRouterCount +
                '}';
    }
}
