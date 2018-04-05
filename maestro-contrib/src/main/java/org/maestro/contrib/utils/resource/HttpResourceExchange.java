/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.maestro.contrib.utils.resource;

import org.maestro.contrib.utils.resource.exceptions.ResourceExchangeException;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Implements network resource exchange via HTTP
 */
public class HttpResourceExchange implements ResourceExchange {
    private static final Logger logger = LoggerFactory.getLogger(HttpResourceExchange.class);

    /**
     * Exchange properties
     */
    public static class Properties {
        /**
         * HTTP Proxy URL
         */
        public static final String HTTP_PROXY = "HTTP_PROXY";

        /**
         * HTTP port
         */
        public static final String PROXY_PORT = "PROXY_PORT";
    }


    private final AbstractHttpClient httpClient = new DefaultHttpClient();


    /**
     * Default constructor: setups a default http client object and uses system
     * proxy information if available
     */
    public HttpResourceExchange() {
        ProxySelectorRoutePlanner routePlanner = new ProxySelectorRoutePlanner(
                httpClient.getConnectionManager().getSchemeRegistry(),
                ProxySelector.getDefault());

        httpClient.setRoutePlanner(routePlanner);
    }

    /**
     * Constructor using connection properties (at the moment, it supports only
     * unauthenticated proxies).
     *
     * @param connectionProperties A hash map of connection properties to use to
     *                             setup the connection (ex.: proxy)
     */
    public HttpResourceExchange(HashMap<String, Object> connectionProperties) {
        String proxy = (String) connectionProperties.get(Properties.HTTP_PROXY);
        Integer port = (Integer) connectionProperties.get(Properties.PROXY_PORT);


        if (proxy != null) {
            HttpHost proxyHost = new HttpHost(proxy, port);

            httpClient.getParams().setParameter(
                    ConnRoutePNames.DEFAULT_PROXY, proxyHost);
        }
    }

    /**
     * Gets the last modified value from the header
     *
     * @param response the HTTP response
     * @return The content length
     */
    private long getLastModified(HttpResponse response) {
        Header header = response.getFirstHeader(HttpHeaders.LAST_MODIFIED);

        if (header == null) {
            logger.warn("The server does not provide the last modified information" +
                    "");

            return 0;
        }

        String tmp = header.getValue();
        //Tue, 26 Jun 2012 02:25:57 GMT
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
        Date date;
        try {
            date = dateFormat.parse(tmp);
        } catch (ParseException e) {
            logger.warn("The last modified date provided by the server is invalid");
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage(), e);
            }

            return 0;
        }

        return date.getTime();
    }

    /**
     * Gets the content length value from the header
     *
     * @param response the HTTP response
     * @return The content length
     */
    private long getContentLength(HttpResponse response) {
        Header header = response.getFirstHeader(HttpHeaders.CONTENT_LENGTH);

        String tmp = header.getValue();
        try {
            return Long.parseLong(tmp);
        } catch (NumberFormatException e) {
            logger.warn("The server provided an invalid content length value");

            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage(), e);
            }
        }

        return 0;
    }


    /*
     * Gets information about a resource
     * @see org.ssps.common.resource.ResourceExchange#get(java.net.URI)
     */
    public ResourceInfo info(URI uri) throws ResourceExchangeException {
        HttpHead httpHead = new HttpHead(uri);
        HttpResponse response;
        try {
            response = httpClient.execute(httpHead);

            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == HttpStatus.SC_OK) {
                long length = getContentLength(response);
                logger.debug("Reading " + length + " bytes from the server");

                ResourceInfo ret = new ResourceInfo();

                ret.setSize(length);
                ret.setLastModified(getLastModified(response));

                return ret;
            } else {
                switch (statusCode) {
                    case HttpStatus.SC_NOT_FOUND:
                        throw new ResourceExchangeException("Remote file not found: " + uri.toString(), uri.toString(),
                                HttpStatus.SC_NOT_FOUND);
                    case HttpStatus.SC_BAD_REQUEST:
                        throw new ResourceExchangeException(
                                "The client sent a bad request", uri.toString(), HttpStatus.SC_NOT_FOUND);
                    case HttpStatus.SC_FORBIDDEN:
                        throw new ResourceExchangeException(
                                "Accessing the resource is forbidden", uri.toString(),
                                HttpStatus.SC_NOT_FOUND);
                    case HttpStatus.SC_UNAUTHORIZED:
                        throw new ResourceExchangeException("Unauthorized", uri.toString(),
                                HttpStatus.SC_NOT_FOUND);
                    case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                        throw new ResourceExchangeException("Internal server error", uri.toString(),
                                HttpStatus.SC_NOT_FOUND);
                    default:
                        throw new ResourceExchangeException(
                                "Unable to download file: http status code " + statusCode, uri.toString(),
                                HttpStatus.SC_NOT_FOUND);
                }
            }
        } catch (ClientProtocolException e) {
            throw new ResourceExchangeException("Unhandled protocol error: "
                    + e.getMessage(), uri.toString(), e);
        } catch (IOException e) {
            throw new ResourceExchangeException("I/O error: " + e.getMessage(), uri.toString(),
                    e);
        }
    }

    /*
     * Gets a resource
     * @see org.ssps.common.resource.ResourceExchange#get(java.net.URI)
     */
    public Resource<InputStream> get(URI uri) throws ResourceExchangeException {
        HttpGet httpget = new HttpGet(uri);
        HttpResponse response;
        try {

            response = httpClient.execute(httpget);

            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    logger.debug("Reading " + entity.getContentLength()
                            + " bytes from the server");

                    Resource<InputStream> ret = new Resource<>();

                    ret.setPayload(entity.getContent());

                    ResourceInfo info = new ResourceInfo();

                    info.setSize(entity.getContentLength());
                    info.setLastModified(getLastModified(response));
                    ret.setResourceInfo(info);

                    return ret;
                }
            } else {
                switch (statusCode) {
                    case HttpStatus.SC_NOT_FOUND:
                        throw new ResourceExchangeException("Remote file not found", uri.toString(), HttpStatus.SC_NOT_FOUND);
                    case HttpStatus.SC_BAD_REQUEST:
                        throw new ResourceExchangeException(
                                "The client sent a bad request", uri.toString(), HttpStatus.SC_BAD_REQUEST);
                    case HttpStatus.SC_FORBIDDEN:
                        throw new ResourceExchangeException(
                                "Accessing the resource is forbidden", uri.toString(), HttpStatus.SC_FORBIDDEN);
                    case HttpStatus.SC_UNAUTHORIZED:
                        throw new ResourceExchangeException("Unauthorized", uri.toString(), HttpStatus.SC_UNAUTHORIZED);
                    case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                        throw new ResourceExchangeException("Internal server error", uri.toString(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    default:
                        throw new ResourceExchangeException(
                                "Unable to download file: http status code " + statusCode, uri.toString());
                }
            }
        } catch (ClientProtocolException e) {
            throw new ResourceExchangeException("Unhandled protocol error: "
                    + e.getMessage(), uri.toString(), e);
        } catch (IOException e) {
            throw new ResourceExchangeException("I/O error: " + e.getMessage(), uri.toString(),
                    e);
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.ssps.common.resource.ResourceExchange#release()
     */
    public void release() {
        if (httpClient != null) {
            httpClient.getConnectionManager().shutdown();
        }
    }

}
