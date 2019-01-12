package org.maestro.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class RoleTest {

    @Test
    public void getCode() {
        assertEquals(0, Role.OTHER.getCode());
    }

    @Test
    public void testFrom() {
        assertEquals(Role.OTHER, Role.from(0));
        assertEquals(Role.SENDER, Role.from(1));
        assertEquals(Role.RECEIVER, Role.from(2));
        assertEquals(Role.INSPECTOR, Role.from(3));
        assertEquals(Role.AGENT, Role.from(4));
        assertEquals(Role.EXPORTER, Role.from(5));
        assertEquals(Role.REPORTS_SERVER, Role.from(6));
        assertEquals(Role.OTHER, Role.from(9999));
    }

    @Test
    public void testToString() {
        assertEquals(HostTypes.OTHER_HOST_TYPE, Role.OTHER.toString());
        assertEquals(HostTypes.SENDER_HOST_TYPE, Role.SENDER.toString());
        assertEquals(HostTypes.RECEIVER_HOST_TYPE, Role.RECEIVER.toString());
        assertEquals(HostTypes.EXPORTER_HOST_TYPE, Role.EXPORTER.toString());
        assertEquals(HostTypes.INSPECTOR_HOST_TYPE, Role.INSPECTOR.toString());
        assertEquals(HostTypes.AGENT_HOST_TYPE, Role.AGENT.toString());
        assertEquals(HostTypes.REPORTS_SERVER_HOST_TYPE, Role.REPORTS_SERVER.toString());
    }

    @Test
    public void isWorker() {
        assertTrue(Role.SENDER.isWorker());
        assertTrue(Role.RECEIVER.isWorker());
        assertFalse(Role.OTHER.isWorker());
        assertFalse(Role.EXPORTER.isWorker());
        assertFalse(Role.INSPECTOR.isWorker());
        assertFalse(Role.AGENT.isWorker());
        assertFalse(Role.REPORTS_SERVER.isWorker());
    }

    @Test
    public void testHostTypeByName() {
        assertEquals(Role.OTHER, Role.hostTypeByName(HostTypes.OTHER_HOST_TYPE));
        assertEquals(Role.SENDER, Role.hostTypeByName(HostTypes.SENDER_HOST_TYPE));
        assertEquals(Role.RECEIVER, Role.hostTypeByName(HostTypes.RECEIVER_HOST_TYPE));
        assertEquals(Role.AGENT, Role.hostTypeByName(HostTypes.AGENT_HOST_TYPE));
        assertEquals(Role.EXPORTER, Role.hostTypeByName(HostTypes.EXPORTER_HOST_TYPE));
        assertEquals(Role.INSPECTOR, Role.hostTypeByName(HostTypes.INSPECTOR_HOST_TYPE));
        assertEquals(Role.REPORTS_SERVER, Role.hostTypeByName(HostTypes.REPORTS_SERVER_HOST_TYPE));
        assertEquals(Role.OTHER, Role.hostTypeByName(""));
        assertEquals(Role.OTHER, Role.hostTypeByName(null));
    }
}