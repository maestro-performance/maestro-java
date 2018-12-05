/*
 Copyright 2012 Otavio Rodolfo Piske

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.maestro.contrib.utils.digest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for message digest implementations
 */
public interface MessageDigest {

    /**
     * Calculates the message digest for a input stream
     * @param inputStream The input stream
     * @return The hexadecimal representation of the message digest
     * @throws IOException If unable to read from the input stream
     */
    String calculate(final InputStream inputStream) throws IOException;

    /**
     * Calculates the message digest for a input stream
     * @param file input file to calculate the message stream
     * @return The hexadecimal representation of the message digest
     * @throws IOException If unable to read the file (ie.: not found)
     */
    String calculate(final File file) throws IOException;

    /**
     * Calculates the message digest for a input stream
     * @param path The path to the file to calculate the digest
     * @return The hexadecimal representation of the message digest
     * @throws IOException If unable to read the file or if unable to read from
     * the input stream
     */
    String calculate(final String path) throws IOException;

    /**
     * Verify the message digest for a file
     * @param source The (path to the) source file
     * @param digest The message digest to verify against
     * @return true if it matches or false otherwise
     * @throws IOException If unable to read the file or if unable to read from
     * the input stream
     */
    boolean verify(final String source, final String digest) throws IOException;

    /**
     * Verify the message digest for a file
     * @param source The (path to the) source file
     * @return true if it matches or false otherwise
     * @throws IOException on I/O errors
     */
    boolean verify(final String source) throws IOException;

    /**
     * Saves the message digest for the source file
     * @param source the (path to the) source file
     * @throws IOException If unable to read the file or if unable to read from
     * the input stream
     */
    void save(final String source) throws IOException;
}
