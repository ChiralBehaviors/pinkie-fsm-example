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
package com.hellblazer.pinkie.buffer.fsmExample;

import java.nio.ByteBuffer;

import com.hellblazer.pinkie.buffer.BufferProtocol;
import com.hellblazer.pinkie.buffer.BufferProtocolHandler;

/**
 * 
 * @author hhildebrand
 * 
 */
public class SimpleProtocolImpl implements SimpleProtocol {
    private class BufferProtocolHandlerImpl implements BufferProtocolHandler {

        @Override
        public void accepted(BufferProtocol bufferProtocol) {
            fsm.accepted(bufferProtocol);
        }

        @Override
        public void closing() {
            fsm.closing();
        }

        @Override
        public void connected(BufferProtocol bufferProtocol) {
            fsm.connected(bufferProtocol);
        }

        @Override
        public ByteBuffer newReadBuffer() {
            return ByteBuffer.allocate(1024);
        }

        @Override
        public ByteBuffer newWriteBuffer() {
            return ByteBuffer.allocate(1024);
        }

        @Override
        public void readError() {
            fsm.readError();
        }

        @Override
        public void readReady(ByteBuffer readBuffer) {
            fsm.readReady(readBuffer);
        }

        @Override
        public void writeError() {
            fsm.writeError();
        }

        @Override
        public void writeReady(ByteBuffer writeBuffer) {
            fsm.writeReady(writeBuffer);
        }
    }

    private final BufferProtocolHandlerImpl handler;
    private final SimpleProtocolContext     fsm;
    @SuppressWarnings("unused")
    private BufferProtocol                  bufferProtocol;

    public SimpleProtocolImpl() {
        fsm = new SimpleProtocolContext(this);
        handler = new BufferProtocolHandlerImpl();
    }

    @Override
    public void send(String msg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBufferProtocol(BufferProtocol bp) {
        bufferProtocol = bp;
    }

    public BufferProtocolHandler getBufferProtocolHandler() {
        return handler;
    }

    @Override
    public void establishSession() {
        // TODO Auto-generated method stub

    }

    @Override
    public void ackReceived(ByteBuffer readBuffer) {
        // TODO Auto-generated method stub

    }
}
