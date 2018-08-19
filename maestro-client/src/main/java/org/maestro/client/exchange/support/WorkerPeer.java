package org.maestro.client.exchange.support;

import org.maestro.common.Role;

import java.util.Objects;

public class WorkerPeer implements PeerInfo {
    private Role role;
    private final String name;
    private final String host;

    private GroupInfo groupInfo = new DefaultGroupInfo("", "all");

    public WorkerPeer(final String name, final String host) {
        this.name = name;
        this.host = host;

        this.role = Role.OTHER;
    }

    protected WorkerPeer(final String name, final String host, final Role role) {
        this.name = name;
        this.host = host;

        this.role = role;
    }

    public WorkerPeer(final Role role, final String name, final String host, final GroupInfo groupInfo) {
        this.role = role;
        this.name = name;
        this.host = host;
        this.groupInfo = groupInfo;
    }

    @Override
    public String peerName() {
        return name;
    }

    @Override
    public String peerHost() {
        return host;
    }

    public GroupInfo groupInfo() {
        return groupInfo;
    }

    @Override
    public Role getRole() {
        return role;
    }

    @Override
    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkerPeer that = (WorkerPeer) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(host, that.host) &&
                Objects.equals(groupInfo, that.groupInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, host, groupInfo);
    }

    @Override
    public String toString() {
        return "WorkerPeer{" +
                "role=" + role +
                ", name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", groupInfo=" + groupInfo +
                '}';
    }
}
