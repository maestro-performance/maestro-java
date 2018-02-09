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

/**
 * Holds resource information
 *
 *
 */
public class ResourceInfo {
	private long size;
	private long lastModified;
	
	/**
	 * Gets the resource size
	 * @return the size in bytes
	 */
	public long getSize() {
		return size;
	}
	
	
	/**
	 * Sets the resource size
	 * @param size the size in bytes
	 */
	public void setSize(long size) {
		this.size = size;
	}
	
	
	/**
	 * Gets the last modified value (in epoch) for the resource
	 * @return the last modified value (in epoch) for the resource
	 */
	public long getLastModified() {
		return lastModified;
	}
	
	
	/**
	 * Sets the last modified value (in epoch) for the resource
	 * @param lastModified the last modified value (in epoch) for the resource
	 */
	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

}
