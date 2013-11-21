/*
 * Copyright (c) 2013 Hal Hildebrand, all rights reserved.
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
package com.hellblazer.pinkie.buffer.fsmExample.fsm;

import com.hellblazer.pinkie.buffer.BufferProtocol;
import com.hellblazer.pinkie.buffer.fsmExample.SimpleProtocol;
import com.hellblazer.tron.Fsm;
import com.hellblazer.tron.InvalidTransition;

/**
 * 
 * @author hhildebrand
 * 
 */
public enum Simple implements SimpleFsm {
    Closed, Connected() {
        @Override
        public SimpleFsm closing() {
            return Closed;
        }

        @Override
        public SimpleFsm readError() {
            return Closed;
        }

        @Override
        public SimpleFsm writeError() {
            return Closed;
        }
    },
    Initial() {
        @Override
        public SimpleFsm accepted(BufferProtocol handler) {
            context().setBufferProtocol(handler, "server");
            fsm().push(SimpleServer.Accepted);
            return Connected;
        }

        @Override
        public SimpleFsm connected(BufferProtocol handler) {
            context().setBufferProtocol(handler, "client");
            fsm().push(SimpleClient.Connected);
            return Connected;
        }
    },
    ProtocolError() {

    };

    private static SimpleProtocol context() {
        SimpleProtocol context = Fsm.thisContext();
        return context;
    }

    private static Fsm<SimpleProtocol, SimpleFsm> fsm() {
        Fsm<SimpleProtocol, SimpleFsm> fsm = Fsm.thisFsm();
        return fsm;
    }

    @Override
    public SimpleFsm accepted(BufferProtocol handler) {
        throw new InvalidTransition();
    }

    @Override
    public SimpleFsm ackReceived() {
        throw new InvalidTransition();
    }

    @Override
    public SimpleFsm close() {
        return Closed;
    }

    @Override
    public SimpleFsm closing() {
        return Closed;
    }

    @Override
    public SimpleFsm connected(BufferProtocol handler) {
        throw new InvalidTransition();
    }

    @Override
    public SimpleFsm goodBye() {
        throw new InvalidTransition();
    }

    @Override
    public SimpleFsm messageProcessed() {
        throw new InvalidTransition();
    }

    @Override
    public SimpleFsm protocolError() {
        return ProtocolError;
    }

    @Override
    public SimpleFsm readError() {
        return Closed;
    }

    @Override
    public SimpleFsm readReady() {
        return ProtocolError;
    }

    @Override
    public SimpleFsm transmitMessage(String message) {
        throw new InvalidTransition();
    }

    @Override
    public SimpleFsm writeError() {
        return ProtocolError;
    }

    @Override
    public SimpleFsm writeReady() {
        return ProtocolError;
    }
}