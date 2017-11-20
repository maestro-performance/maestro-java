import net.orpiske.mpt.reports.ReportGenerator

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

@GrabConfig(systemClassLoader=true)

@Grab(group='org.apache.commons', module='commons-lang3', version='3.6')

@Grab(group='net.orpiske', module='maestro-common', version='1.2.0-SNAPSHOT')

import net.orpiske.mpt.reports.ReportGenerator
import net.orpiske.mpt.common.LogConfigurator

LogConfigurator.verbose()
ReportGenerator.generate(args[0])