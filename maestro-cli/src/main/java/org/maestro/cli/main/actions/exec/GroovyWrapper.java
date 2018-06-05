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
package org.maestro.cli.main.actions.exec;

import groovy.lang.GroovyShell;

import java.io.File;
import java.io.IOException;


public class GroovyWrapper {
    public void run(final File file, final String[] args) throws IOException {

        GroovyShell groovyShell = new GroovyShell(this.getClass().getClassLoader());

        groovyShell.run(file, args);
    }
}
