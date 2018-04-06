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
package org.maestro.cli.main.actions;


import org.maestro.common.Constants;
import org.maestro.common.LogConfigurator;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;


/**
 * Implements app specific actions
 */
public abstract class Action {
	
	/**
	 * Prints the help for the action and exit
	 * @param options the options object
	 * @param code the exit code
	 */
	protected void help(final Options options, int code) {
		HelpFormatter formatter = new HelpFormatter();

		formatter.printHelp(Constants.BIN_NAME, options);
		System.exit(code);
	}

	protected void configureLogLevel(final String logLevel) {
		switch (logLevel) {
			case "trace": {
				LogConfigurator.trace();
				break;
			}
			case "debug": {
				LogConfigurator.debug();
				break;
			}
			case "warn": {
				LogConfigurator.silent();
				break;
			}
			case "info":
			default: {
				LogConfigurator.verbose();
				break;
			}
		}
	}
	
	/**
	 * Process the command line arguments
	 * @param args the command line arguments
	 */
	protected abstract void processCommand(String[] args);
	
	/**
	 * Runs the action
	 * @return the exit code
	 */
	public abstract int run();
	

}
