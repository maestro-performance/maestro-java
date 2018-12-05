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

import org.maestro.common.client.notes.*;
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
        LocationTypeInfo ret = new LocationTypeInfo(unpacker.unpackInt());

        ret.setIndex(unpacker.unpackInt());

        return ret;
    }

    public static void pack(final MessageBufferPacker packer, final LocationTypeInfo locationTypeInfo) throws IOException {
        packer.packInt(locationTypeInfo.getFileCount());
        packer.packInt(locationTypeInfo.getIndex());
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

    public static ErrorCode unpackErrorCode(final MessageUnpacker unpacker) throws IOException {
        final int value = unpacker.unpackInt();

        return ErrorCode.from(value);
    }

    public static void pack(final MessageBufferPacker packer, final ErrorCode errorCode) throws IOException {
        packer.packInt(errorCode.getCode());
    }


    public static SutDetails unpackSutDetails(final MessageUnpacker unpacker) throws IOException {
        int sutId = unpacker.unpackInt();
        String sutName = unpacker.unpackString();
        String sutVersion = unpacker.unpackString();
        String sutJvmVersion = unpacker.unpackString();
        String sutOtherInfo = unpacker.unpackString();
        String sutTags = unpacker.unpackString();
        String labName = unpacker.unpackString();
        String testTags = unpacker.unpackString();

        return new SutDetails(sutId, sutName, sutVersion, sutJvmVersion, sutOtherInfo, sutTags, labName, testTags);
    }

    public static void pack(final MessageBufferPacker packer, final SutDetails sutDetails) throws IOException {
        packer.packInt(sutDetails.getSutId());
        packer.packString(sutDetails.getSutName());
        packer.packString(sutDetails.getSutVersion());
        packer.packString(sutDetails.getSutJvmVersion());
        packer.packString(sutDetails.getSutOtherInfo());
        packer.packString(sutDetails.getSutTags());
        packer.packString(sutDetails.getLabName());
        packer.packString(sutDetails.getTestTags());
    }

    public static TestExecutionInfo unpackTestExecutionInfo(final MessageUnpacker unpacker) throws IOException {
        Test test = SerializationUtils.unpackTest(unpacker);

        boolean hasDetails = unpacker.unpackBoolean();

        SutDetails sutDetails = null;

        if (hasDetails) {
            sutDetails = SerializationUtils.unpackSutDetails(unpacker);
        }

        return new TestExecutionInfo(test, sutDetails);
    }

    public static void pack(final MessageBufferPacker packer, final TestExecutionInfo testExecutionInfo) throws IOException {
        pack(packer, testExecutionInfo.getTest());

        packer.packBoolean(testExecutionInfo.hasSutDetails());
        if (testExecutionInfo.hasSutDetails()) {
            pack(packer, testExecutionInfo.getSutDetails());
        }
    }
}
