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

package org.maestro.client.notes;

import org.maestro.common.client.notes.LocationTypeInfo;
import org.maestro.common.client.notes.Test;
import org.maestro.common.client.notes.TestDetails;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

class SerializationUtils {

    public static Test unpackTest(final MessageUnpacker unpacker) throws IOException {
        int testNumber = unpacker.unpackInt();
        int testIteration = unpacker.unpackInt();
        String testName = unpacker.unpackString();
        String scriptName = unpacker.unpackString();

        final TestDetails testDetails = unpackTestDetails(unpacker);

        return new Test(testNumber, testIteration, testName, scriptName, testDetails);
    }

    public static void pack(final MessageBufferPacker packer, final Test test) throws IOException {
        packer.packInt(test.getTestNumber());
        packer.packInt(test.getTestIteration());
        packer.packString(test.getTestName());
        packer.packString(test.getScriptName());

        pack(packer, test.getTestDetails());
    }

    public static LocationTypeInfo unpackLocationTypeInfo(final MessageUnpacker unpacker) throws IOException {
        LocationTypeInfo ret = new LocationTypeInfo();

        ret.setIndex(unpacker.unpackInt());
        ret.setFileCount(unpacker.unpackInt());

        return ret;
    }

    public static void pack(final MessageBufferPacker packer, final LocationTypeInfo locationTypeInfo) throws IOException {
        packer.packInt(locationTypeInfo.getIndex());
        packer.packInt(locationTypeInfo.getFileCount());
    }

    public static TestDetails unpackTestDetails(final MessageUnpacker unpacker) throws IOException {
        TestDetails ret = new TestDetails();

        ret.setTestDescription(unpacker.unpackString());
        ret.setTestComments(unpacker.unpackString());

        return ret;
    }


    public static void pack(final MessageBufferPacker packer, final TestDetails testDetails) throws IOException {
        packer.packString(testDetails.getTestDescription());
        packer.packString(testDetails.getTestComments());
    }
}
