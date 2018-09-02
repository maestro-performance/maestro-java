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

package org.maestro.plotter.amqp.inspector.connections;

import org.maestro.plotter.common.InstantRecord;

import java.time.Instant;
import java.util.Objects;

/**
 * A class represents single record from csv file
 */
public class ConnectionsRecord implements Comparable<ConnectionsRecord>, InstantRecord {
    private Instant timestamp;
    private String name;
    private String host;
    private String role;
    private String dir;
    private String opened;
    private String identity;
    private String user;
    private String sasl;
    private String encrypted;
    private String sslProto;
    private String sslCipher;
    private String tenant;
    private String authenticated;
    private String properties;

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

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getOpened() {
        return opened;
    }

    public void setOpened(String opened) {
        this.opened = opened;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSasl() {
        return sasl;
    }

    public void setSasl(String sasl) {
        this.sasl = sasl;
    }

    public String getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(String encrypted) {
        this.encrypted = encrypted;
    }

    public String getSslProto() {
        return sslProto;
    }

    public void setSslProto(String sslProto) {
        this.sslProto = sslProto;
    }

    public String getSslCipher() {
        return sslCipher;
    }

    public void setSslCipher(String sslCipher) {
        this.sslCipher = sslCipher;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(String authenticated) {
        this.authenticated = authenticated;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    @Override
    public int compareTo(ConnectionsRecord routerLinkRecord) {
        return this.getTimestamp().compareTo(routerLinkRecord.getTimestamp());
    }



    @Override
    public int hashCode() {
        return Objects.hash(timestamp, name, host, role, dir, opened, identity, user, sasl, encrypted, sslProto,
                sslCipher, tenant, authenticated, properties);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionsRecord that = (ConnectionsRecord) o;
        return Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(name, that.name) &&
                Objects.equals(host, that.host) &&
                Objects.equals(role, that.role) &&
                Objects.equals(dir, that.dir) &&
                Objects.equals(opened, that.opened) &&
                Objects.equals(identity, that.identity) &&
                Objects.equals(user, that.user) &&
                Objects.equals(sasl, that.sasl) &&
                Objects.equals(encrypted, that.encrypted) &&
                Objects.equals(sslProto, that.sslProto) &&
                Objects.equals(sslCipher, that.sslCipher) &&
                Objects.equals(tenant, that.tenant) &&
                Objects.equals(authenticated, that.authenticated) &&
                Objects.equals(properties, that.properties);
    }

    @Override
    public String toString() {
        return "RouterLinkRecord{" +
                "timestamp=" + timestamp +
                ", name='" + name + '\'' +
                ", host=" + host +
                ", role=" + role +
                ", dir=" + dir +
                ", opened=" + opened +
                ", identity=" + identity +
                ", user=" + user +
                ", sasl=" + sasl +
                ", encrypted=" + encrypted +
                ", sslProto=" + sslProto +
                ", sslCipher=" + sslCipher +
                ", tenant=" + tenant +
                ", authenticated=" + authenticated +
                ", properties=" + properties +
                '}';
    }
}
