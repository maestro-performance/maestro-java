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

package org.maestro.contrib.groovy;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

public class GroovyCallbackWalkerTest {
    private List<File> files;


    @Before
    public void setUp() throws Exception {
        String filePath = this.getClass().getResource("/groovy").getFile();
        File file = new File(filePath);

        GroovyCallbackWalker cb = new GroovyCallbackWalker();
        files = cb.load(file);
    }

    @Test
    public void testLoad() {
        assertNotNull(files);
        assertEquals("Invalid number of files", 2, files.size());

        assertEquals(1L, files.stream().filter(f -> f.getName().equals("Empty.groovy")).count());
        assertEquals(1L, files.stream().filter(f -> f.getName().equals("Other.groovy")).count());
        assertEquals(0L, files.stream().filter(f -> f.getName().equals("ShouldBeIgnored.groovy")).count());
    }
}