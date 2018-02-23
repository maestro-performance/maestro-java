package org.maestro.common;

import org.apache.commons.lang3.StringUtils;
import org.maestro.common.exceptions.MaestroException;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Maestro URL utilities
 */
public class URLUtils {

    private URLUtils() {}

    /**
     * The client uses the mqtt://{host} url format so it's the same as the C client. This
     * method ensures that the URLs follows this format.
     * @param url the URL to sanitize
     * @return the sanitized URL
     */
    public static String sanitizeURL(final String url) {
        return StringUtils.replace(url, "mqtt", "tcp");
    }


    /**
     * Get the host part from a URL
     * @param string the URL (ie.: http://hostname:port)
     * @return the host part of the URL
     */
    public static String getHostnameFromURL(final String string) {
        try {
            URL url = new URL(string);

            return url.getHost();
        } catch (MalformedURLException e) {
            throw new MaestroException("Invalid URL");
        }
    }
}
