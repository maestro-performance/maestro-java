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
package org.maestro.contrib.utils.resource.exceptions;

/**
 * Network exchanges exception
 */
@SuppressWarnings("serial")
public class ResourceExchangeException extends Exception {
    private int code;
    private final String url;

    /**
     * Constructor
     *
     * @param message error message
     * @param url URL that caused the exception
     * @param t       root cause
     */
    public ResourceExchangeException(final String message, final String url, Throwable t) {
        super(message, t);

        this.url = url;
    }

    /**
     * Constructor
     *
     * @param message error message
     * @param url URL that caused the exception
     * @param code error code
     */
    public ResourceExchangeException(final String message, final String url, final int code) {
        super(message);

        this.url = url;
        this.code = code;
    }


    /**
     * Constructor
     *
     * @param message error message
     * @param url URL that caused the exception
     */
    public ResourceExchangeException(final String message, final String url) {
        super(message);

        this.url = url;
    }

    /**
     * Get the error code
     * @return the error code
     */
    public int getCode() {
        return code;
    }


    /**
     * Get the URL that caused the exception
     * @return the URL as a string
     */
    public String getUrl() {
        return url;
    }
}
