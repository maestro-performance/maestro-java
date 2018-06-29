package org.maestro.reports.organizer;

import org.maestro.common.NodeUtils;

public class NodeOrganizer extends DefaultOrganizer {
    public NodeOrganizer(final String baseDir) {
        super(baseDir);
    }

    @Override
    public String organize(final String peer, final String hostType) {
        String host = NodeUtils.getHostFromName(peer);

        return combine(hostType, host);
    }
}
