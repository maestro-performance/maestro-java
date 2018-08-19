/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.maestro.contrib.utils.digest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;

public class Sha1Digest implements MessageDigest {
    private static final Logger logger = LoggerFactory.getLogger(Sha1Digest.class);

    private static final String HASH_NAME = "sha1";

    public String calculate(final InputStream inputStream) throws IOException {
        logger.trace("Calculating message digest");
        return DigestUtils.shaHex(inputStream);
    }

    public String calculate(final File file) throws IOException {
        InputStream fileInputStream = null;

        try {
            fileInputStream = new BufferedInputStream(new FileInputStream(file));

            return calculate(fileInputStream);
        }
        finally {
            IOUtils.closeQuietly(fileInputStream);
        }
    }


    /* (non-Javadoc)
     * @see org.ssps.common.digest.MessageDigest#calculate(java.lang.String)
     */
    public String calculate(String path) throws IOException {
        File file = new File(path);

        return calculate(file);
    }

    /* (non-Javadoc)
     * @see org.ssps.common.digest.MessageDigest#verify(java.lang.String)
     */
    public boolean verify(String source) throws IOException {
        InputStream stream = null;

        try {
            stream = new FileInputStream(source + "." + HASH_NAME);

            String digest = IOUtils.toString(stream);

            digest = digest.trim();
            return verify(source, digest);
        }
        finally {
            IOUtils.closeQuietly(stream);
        }
    }


    /* (non-Javadoc)
     * @see org.ssps.common.digest.MessageDigest#verify(java.lang.String, java.lang.String)
     */
    public boolean verify(String source, String digest) throws IOException {
        logger.trace("Verifying message digest");
        String sourceDigest;

        sourceDigest = calculate(source);

        return sourceDigest.equals(digest);

    }

    /* (non-Javadoc)
     * @see org.ssps.common.digest.MessageDigest#save(java.lang.String)
     */
    public void save(String source) throws IOException {
        String digest;
        FileOutputStream output = null;

        try {
            digest = calculate(source);

            File file = new File(source + "." + HASH_NAME);

            if (file.exists()) {
                if (!file.delete()) {
                    throw new IOException("Unable to delete an existent file: "
                            + file.getPath());
                }
            }
            else {
                if (!file.createNewFile()) {
                    throw new IOException("Unable to create a new file: "
                            + file.getPath());
                }
            }

            output = new FileOutputStream(file);
            IOUtils.write(digest, output);
        }
        finally {
            IOUtils.closeQuietly(output);
        }
    }
}