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
 *
 *
 */
public final  class Resource<T> {
	private T payload;
	private ResourceInfo resourceInfo;

	public T getPayload() {
		return payload;
	}
	
	public void setPayload(T payload) {
		this.payload = payload;
	}
	
	public ResourceInfo getResourceInfo() {
		return resourceInfo;
	}
	
	public void setResourceInfo(ResourceInfo resourceInfo) {
		this.resourceInfo = resourceInfo;
	}

	

}
