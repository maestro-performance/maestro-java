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

import java.io.InputStream;
import java.net.URI;

/**
 * Defines a simple interface for VFS-based exchanges
 *
 *
 */
public interface ResourceExchange {
	
	/**
	 * Gets the resource pointed by an URL into file
	 * @param uri the resource location/address
	 * @throws ResourceExchangeException if unable to obtain the resource.
	 * Check the root cause for details.
	 */
	Resource<InputStream> get(final URI uri) throws ResourceExchangeException;
	
	
	/**
	 * Gets the resource information pointed by an URL into file
	 * @param uri the resource location/address
	 * @throws ResourceExchangeException if unable to obtain the resource. 
	 * Check the root cause for details.
	 */
	ResourceInfo info(final URI uri) throws ResourceExchangeException;
	
	
	/**
	 * Releases any open resources used by the exchange
	 */
	void release();
}
