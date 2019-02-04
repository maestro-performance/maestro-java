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

package org.maestro.contrib.utils.net;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class URLUtilsTest {
    @Test
    public void testGetFileName() throws MalformedURLException, URISyntaxException {
        assertEquals("The expected file name does not match", "somefile.report", URLUtils.getFilename("http://localhost/somefile.report"));
        assertEquals("The expected file name does not match", "somefile.report", URLUtils.getFilename("http://localhost/with/path/somefile.report"));
        assertEquals("The expected file name does not match", "", URLUtils.getFilename("http://localhost/"));
        assertEquals("The expected file name does not match", "", URLUtils.getFilename("http://localhost"));
        assertEquals("The expected file name does not match", "", URLUtils.getFilename("http://localhost/with/path/somefile.report/"));
    }


    @Test
    public void testGetFileNameWithParameters() throws MalformedURLException, URISyntaxException {
        assertEquals("The expected file name does not match", "somefile.report", URLUtils.getFilename("http://localhost/somefile.report?something=otherthing"));
        assertEquals("The expected file name does not match", "somefile.report", URLUtils.getFilename("http://localhost/with/path/somefile.report?something=otherthing"));
        assertEquals("The expected file name does not match", "", URLUtils.getFilename("http://localhost/?something=otherthing"));
        assertEquals("The expected file name does not match", "", URLUtils.getFilename("http://localhost/with/path/somefile.report/?something=otherthing"));
    }

    @Test(expected = MalformedURLException.class)
    public void testNullURLStr() throws MalformedURLException, URISyntaxException {
        assertEquals("The expected file name does not match", "", URLUtils.getFilename((String) null));
    }

    @Test(expected = MalformedURLException.class)
    public void testNullURL() throws MalformedURLException, URISyntaxException {
        assertEquals("The expected file name does not match", "", URLUtils.getFilename((URI) null));
    }
}