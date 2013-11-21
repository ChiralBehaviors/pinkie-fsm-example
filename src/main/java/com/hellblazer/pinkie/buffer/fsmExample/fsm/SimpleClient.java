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
import com.hellblazer.tron.Entry;
import com.hellblazer.tron.Fsm;
import com.hellblazer.tron.InvalidTransition;

/**
 * 
 * @author hhildebrand
 * 
 */
public enum SimpleClient implements SimpleFsm {
    AckMessage() {
        @Override
        public SimpleFsm ackReceived() {
            return SendMessage;
        }

        @Entry
        public void entry() {
            context().ackReceived();
        }
    },
    AwaitAck() {
        @Entry
        public void entry() {
            context().awaitAck();
        }

        @Override
        public SimpleFsm readReady() {
            return AckMessage;
        }
    },
    Connected() {
        @Entry
        public void establishClientSession() {
            context().establishClientSession();
        }

        @Override
        public SimpleFsm writeReady() {
            return EstablishSession;
        }
    },
    EstablishSession() {
        @Entry
        public void entry() {
            context().awaitAck();
        }

        @Override
        public SimpleFsm readReady() {
            context().enableSend();
            return SendMessage;
        }
    },
    MessageSent() {
        @Override
        public SimpleFsm writeReady() {
            return AwaitAck;
        }
    },
    SendGoodbye {
        @Override
        public SimpleFsm closing() {
            fsm().pop().closing();
            return null;
        }

        @Entry
        public void entry() {
            context().sendGoodbye();
        }

        @Override
        public SimpleFsm writeReady() {
            return null;
        }
    },
    SendMessage() {

        @Override
        public SimpleFsm transmitMessage(String message) {
            context().transmitMessage(message);
            return MessageSent;
        }
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
        fsm().pop().protocolError();
        context().logProtocolError("accepted");
        return null;
    }

    @Override
    public SimpleFsm ackReceived() {
        throw new InvalidTransition();
    }

    @Override
    public SimpleFsm close() {
        fsm().pop().closing();
        return null;
    }

    @Override
    public SimpleFsm closing() {
        fsm().pop().closing();
        return null;
    }

    @Override
    public SimpleFsm connected(BufferProtocol handler) {
        fsm().pop().protocolError();
        return null;
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
        SimpleFsm popTransition = fsm().pop();
        popTransition.protocolError();
        return null;
    }

    @Override
    public SimpleFsm readError() {
        fsm().pop().protocolError();
        context().logProtocolError("readError");
        return null;
    }

    @Override
    public SimpleFsm readReady() {
        fsm().pop().protocolError();
        context().logProtocolError("readReady");
        return null;
    }

    @Override
    public SimpleFsm transmitMessage(String message) {
        throw new InvalidTransition();
    }

    @Override
    public SimpleFsm writeError() {
        fsm().pop().protocolError();
        context().logProtocolError("writeError");
        return null;
    }

    @Override
    public SimpleFsm writeReady() {
        fsm().pop().protocolError();
        context().logProtocolError("writeReady");
        return null;
    }
}