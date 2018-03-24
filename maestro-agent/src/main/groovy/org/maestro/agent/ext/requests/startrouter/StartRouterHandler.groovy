package org.maestro.agent.ext.requests.startrouter

import org.maestro.agent.base.AbstractHandler

class StartRouterHandler extends AbstractHandler {

    @Override
    Object handle() {
        System.out.println("Start Router note received");

        def process = "pwd".execute()

        if(!process)
            System.out.println("Wrong exit code")
        else
            process.text.eachLine {println it}

        return null
    }
}