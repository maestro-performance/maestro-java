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

package org.maestro.client.exchange;

import org.maestro.client.notes.*;
import org.maestro.client.notes.InternalError;
import org.maestro.common.client.exceptions.MalformedNoteException;
import org.maestro.common.client.notes.MaestroCommand;
import org.maestro.common.client.notes.MaestroNote;
import org.maestro.common.client.notes.MaestroNoteType;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class MaestroDeserializer {
    private static final Logger logger = LoggerFactory.getLogger(MaestroDeserializer.class);

    private static MaestroNotification deserializeNotification(MessageUnpacker unpacker) throws IOException, MalformedNoteException {
        long tmpCommand = unpacker.unpackLong();
        MaestroCommand command = MaestroCommand.from(tmpCommand);

        switch (Objects.requireNonNull(command)) {
            case MAESTRO_NOTE_NOTIFY_FAIL: {
                return new TestFailedNotification(unpacker);
            }
            case MAESTRO_NOTE_NOTIFY_SUCCESS: {
                return new TestSuccessfulNotification(unpacker);
            }
            case MAESTRO_NOTE_ABNORMAL_DISCONNECT: {
                return new AbnormalDisconnect(unpacker);
            }
            case MAESTRO_NOTE_NOTIFY_DRAIN_COMPLETE: {
                return new DrainCompleteNotification(unpacker);
            }
            case MAESTRO_NOTE_START_TEST: {
                return new StartTestNotification(unpacker);
            }
            default: {
                throw new MalformedNoteException("Invalid notification command: " + tmpCommand);
           }
        }
    }

    private static MaestroData deserializeData(MessageUnpacker unpacker) throws IOException, MalformedNoteException {
        long tmpCommand = unpacker.unpackLong();
        MaestroCommand command = MaestroCommand.from(tmpCommand);

        switch (Objects.requireNonNull(command)) {
            case MAESTRO_NOTE_LOG: {
                LogResponseJoiner instance = LogResponseJoiner.getInstance();

                return instance.join(new LogResponse(unpacker));
            }
            default: {
                logger.error("Type unknown: {}", command.getClass());
                throw new MalformedNoteException("Invalid response command: " + tmpCommand);
            }
        }
    }

    private static MaestroResponse deserializeResponse(MessageUnpacker unpacker) throws IOException, MalformedNoteException {
        long tmpCommand = unpacker.unpackLong();
        MaestroCommand command = MaestroCommand.from(tmpCommand);

        switch (Objects.requireNonNull(command)) {
            case MAESTRO_NOTE_OK: {
                return new OkResponse(unpacker);
            }
            case MAESTRO_NOTE_PING: {
                return new PingResponse(unpacker);
            }
            case MAESTRO_NOTE_INTERNAL_ERROR: {
                return new InternalError(unpacker);
            }
            case MAESTRO_NOTE_PROTOCOL_ERROR: {
                return new ProtocolError();
            }
            case MAESTRO_NOTE_STATS: {
                return new StatsResponse(unpacker);
            }
            case MAESTRO_NOTE_GET: {
                return new GetResponse(unpacker);
            }
            case MAESTRO_NOTE_USER_COMMAND_1: {
                return new UserCommand1Response(unpacker);
            }
            case MAESTRO_NOTE_START_WORKER:
            case MAESTRO_NOTE_STOP_WORKER:
            case MAESTRO_NOTE_START_INSPECTOR:
            case MAESTRO_NOTE_STOP_INSPECTOR:
            case MAESTRO_NOTE_SET:
            case MAESTRO_NOTE_HALT:
            case MAESTRO_NOTE_START_AGENT:
            case MAESTRO_NOTE_STOP_AGENT:{
                logger.warn("Unexpected maestro command for a response: {}", tmpCommand);
                throw new MalformedNoteException("Invalid response command: " + tmpCommand);
            }
            default: {
                logger.error("Type unknown: {}", command.getClass());
                throw new MalformedNoteException("Invalid response command: " + tmpCommand);
            }
        }

    }

    private static MaestroRequest deserializeRequest(MessageUnpacker unpacker) throws IOException, MalformedNoteException {
        long tmpCommand = unpacker.unpackLong();
        MaestroCommand command = MaestroCommand.from(tmpCommand);

        switch (Objects.requireNonNull(command)) {
            case MAESTRO_NOTE_PING: {
                return new PingRequest(unpacker);
            }
            case MAESTRO_NOTE_STATS: {
                return new StatsRequest();
            }
            case MAESTRO_NOTE_START_WORKER: {
                return new StartWorker(unpacker);
            }
            case MAESTRO_NOTE_STOP_WORKER: {
                return new StopWorker();
            }
            case MAESTRO_NOTE_START_INSPECTOR: {
                return new StartInspector(unpacker);
            }
            case MAESTRO_NOTE_STOP_INSPECTOR: {
                return new StopInspector();
            }
            case MAESTRO_NOTE_SET: {
                return new SetRequest(unpacker);
            }
            case MAESTRO_NOTE_HALT: {
                return new Halt();
            }
            case MAESTRO_NOTE_GET: {
                return new GetRequest(unpacker);
            }
            case MAESTRO_NOTE_START_AGENT: {
                return new StartAgent();
            }
            case MAESTRO_NOTE_STOP_AGENT: {
                return new StopAgent();
            }
            case MAESTRO_NOTE_USER_COMMAND_1: {
                return new UserCommand1Request(unpacker);
            }
            case MAESTRO_NOTE_AGENT_SOURCE: {
                return new AgentSourceRequest(unpacker);
            }
            case MAESTRO_NOTE_LOG: {
                return new LogRequest(unpacker);
            }
            case MAESTRO_NOTE_DRAIN: {
                return new DrainRequest(unpacker);
            }
            case MAESTRO_NOTE_GROUP_JOIN: {
                return new GroupJoinRequest(unpacker);
            }
            case MAESTRO_NOTE_GROUP_LEAVE: {
                return new GroupLeaveRequest(unpacker);
            }
            case MAESTRO_NOTE_ROLE_ASSIGN: {
                return new RoleAssign(unpacker);
            }
            case MAESTRO_NOTE_ROLE_UNASSIGN: {
                return new RoleUnassign(unpacker);
            }
            case MAESTRO_NOTE_START_TEST: {
                return new StartTestRequest(unpacker);
            }
            case MAESTRO_NOTE_STOP_TEST: {
                return new StopTestRequest(unpacker);
            }
            default: {
                throw new MalformedNoteException("Invalid request command: " + tmpCommand);
            }
        }
    }

    public static MaestroEvent deserializeEvent(byte[] bytes) throws IOException, MalformedNoteException {
        try (MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(bytes)){
            final short tmpType = unpacker.unpackShort();
            final MaestroNoteType type = MaestroNoteType.from(tmpType);

            switch (Objects.requireNonNull(type)) {
                case MAESTRO_TYPE_REQUEST:
                    return deserializeRequest(unpacker);
                case MAESTRO_TYPE_NOTIFICATION:
                    return deserializeNotification(unpacker);
                case MAESTRO_TYPE_DATA:
                    return deserializeData(unpacker);
                default:
                    throw new MalformedNoteException("Invalid event type: " + tmpType);
            }
        }
    }

    public static MaestroNote deserialize(byte[] bytes) throws IOException, MalformedNoteException {
        try (MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(bytes)){
            final short tmpType = unpacker.unpackShort();
            final MaestroNoteType type = MaestroNoteType.from(tmpType);

            switch (Objects.requireNonNull(type)) {
                case MAESTRO_TYPE_REQUEST:
                    return deserializeRequest(unpacker);
                case MAESTRO_TYPE_RESPONSE:
                    return deserializeResponse(unpacker);
                case MAESTRO_TYPE_NOTIFICATION:
                    return deserializeNotification(unpacker);
                default:
                    throw new MalformedNoteException("Invalid note type: " + tmpType);
            }
        }
    }
}
