package net.orpiske.mpt.maestro.driver.quiver;

import net.ssorj.quiver.QuiverArrowJms;

public class QuiverDriver {

    /*
    usage: quiver [-h] [-m COUNT] [--impl NAME] [--body-size COUNT] [--credit COUNT]
              [--timeout SECONDS] [--output DIRECTORY] [--init-only] [--quiet]
              [--verbose] ADDRESS
     */

    public void load() {

        String[] args = {""};

        try {
            QuiverArrowJms.doMain(args);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public int run() {
        return 0;
    }


}
