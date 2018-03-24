package org.maestro.agent.ext.requests.stoprouter

import org.maestro.agent.base.AbstractHandler

class StopRouterHandler extends AbstractHandler {

    @Override
    Object handle() {
        System.out.println("Stop Router note received");
        return null
    }
}