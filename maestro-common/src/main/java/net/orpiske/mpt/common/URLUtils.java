package net.orpiske.mpt.common;

import org.apache.commons.lang3.StringUtils;

/**
 * Maestro URL utilities
 */
public class URLUtils {

    private URLUtils() {}

    /**
     * The client uses the mqtt://{host} url format so it's the same as the C client. This
     * method ensures that the URLs follows this format.
     * @param url
     * @return
     */
    public static String sanizeURL(final String url) {
        return StringUtils.replace(url, "mqtt", "tcp");
    }
}
