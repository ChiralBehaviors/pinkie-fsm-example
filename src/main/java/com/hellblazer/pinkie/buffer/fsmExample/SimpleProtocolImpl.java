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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hellblazer.pinkie.buffer.BufferProtocol;
import com.hellblazer.pinkie.buffer.BufferProtocolHandler;
import com.hellblazer.pinkie.buffer.fsmExample.fsm.Simple;
import com.hellblazer.pinkie.buffer.fsmExample.fsm.SimpleFsm;
import com.hellblazer.tron.Fsm;
import com.hellblazer.utils.Gate;

/**
 * 
 * @author hhildebrand
 * 
 */
public class SimpleProtocolImpl implements SimpleProtocol {
    public static interface MessageHandler {

        void handle(String message);
    }

    private class BufferProtocolHandlerImpl implements BufferProtocolHandler {

        @Override
        public void accepted(BufferProtocol bufferProtocol) {
            fsm.getTransitions().accepted(bufferProtocol);
        }

        @Override
        public void closing() {
            fsm.getTransitions().closing();
        }

        @Override
        public void connected(BufferProtocol bufferProtocol) {
            fsm.getTransitions().connected(bufferProtocol);
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
            fsm.getTransitions().readError();
        }

        @Override
        public void readReady() {
            fsm.getTransitions().readReady();
        }

        @Override
        public void writeError() {
            fsm.getTransitions().writeError();
        }

        @Override
        public void writeReady() {
            fsm.getTransitions().writeReady();
        }
    }

    private final BufferProtocolHandlerImpl      handler;
    private final Fsm<SimpleProtocol, SimpleFsm> fsm;

    private BufferProtocol                       bufferProtocol;

    private static final Logger                  LOG = LoggerFactory.getLogger(SimpleProtocolImpl.class);

    private Gate                                 sendGate;

    private final MessageHandler                 messageHandler;

    public SimpleProtocolImpl(MessageHandler messageHandler) {
        fsm = Fsm.construct((SimpleProtocol) this, SimpleFsm.class,
                            Simple.Initial, true);
        handler = new BufferProtocolHandlerImpl();
        sendGate = new Gate();
        this.messageHandler = messageHandler;
    }

    @Override
    public void ackReceived() {
        ByteBuffer readBuffer = bufferProtocol.getReadBuffer();
        readBuffer.rewind();
        byte type = readBuffer.get();

        if (type == (byte) MessageType.ACK.ordinal()) {
            fsm.getTransitions().ackReceived();
        } else {
            LOG.error("Message type should be ACK but is {} instead",
                      MessageType.values()[type]);
            fsm.getTransitions().protocolError();
        }

    }

    /* (non-Javadoc)
     * @see com.hellblazer.pinkie.buffer.fsmExample.SimpleProtocol#awaitAck()
     */
    @Override
    public void awaitAck() {
        ByteBuffer readBuffer = bufferProtocol.getReadBuffer();
        readBuffer.clear();
        readBuffer.limit(1);
        bufferProtocol.selectForRead();

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.hellblazer.pinkie.buffer.fsmExample.SimpleProtocol#awaitMessage()
     */
    @Override
    public void awaitMessage() {
        ByteBuffer readBuffer = bufferProtocol.getReadBuffer();
        readBuffer.clear();
        bufferProtocol.selectForRead();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.hellblazer.pinkie.buffer.fsmExample.SimpleProtocol#awaitSession()
     */
    @Override
    public void awaitSession() {
        ByteBuffer readBuffer = bufferProtocol.getReadBuffer();
        readBuffer.limit(1);
        readBuffer.rewind();

        // waiting for stuff to read...
        bufferProtocol.selectForRead();

    }

    public void close() {
        fsm.getTransitions().close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.hellblazer.pinkie.buffer.fsmExample.SimpleProtocol#enableSend()
     */
    @Override
    public void enableSend() {
        sendGate.open();
    }

    @Override
    public void establishClientSession() {
        ByteBuffer buffer = bufferProtocol.getWriteBuffer();
        buffer.clear();
        buffer.put((byte) MessageType.ESTABLISH.ordinal());
        buffer.flip();
        ByteBuffer readBuffer = bufferProtocol.getReadBuffer();
        readBuffer.limit(1);
        readBuffer.rewind();
        bufferProtocol.selectForWrite();

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.hellblazer.pinkie.buffer.fsmExample.SimpleProtocol#establishServerSession
     * ()
     */
    @Override
    public void establishServerSession() {
        ByteBuffer readBuffer = bufferProtocol.getReadBuffer();
        readBuffer.rewind();
        byte type = readBuffer.get();

        if (type != (byte) MessageType.ESTABLISH.ordinal()) {
            LOG.error("Message type should be ESTABLISH but is {} instead",
                      MessageType.values()[type]);
            fsm.getTransitions().protocolError();
            return;
        }
        ByteBuffer buffer = bufferProtocol.getWriteBuffer();
        buffer.rewind();
        buffer.limit(1);
        buffer.put((byte) MessageType.ACK.ordinal());
        bufferProtocol.setReadFullBuffer(false);
        buffer.flip();
        bufferProtocol.selectForWrite();

    }

    /**
     * @return
     */
    public SimpleFsm getCurrentState() {
        return fsm.getCurrentState();
    }

    @Override
    public void logProtocolError(String message) {
        LOG.error("PROTOCOL ERROR: Transition: {}, FSM: {}", message, fsm);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.hellblazer.pinkie.buffer.fsmExample.SimpleProtocol#processMessage()
     */
    @Override
    public void processMessage() {
        ByteBuffer readBuffer = bufferProtocol.getReadBuffer();
        readBuffer.mark();
        readBuffer.position(0);
        byte type = readBuffer.get(0);

        if (type == (byte) MessageType.MSG.ordinal()) {
            readMessage(readBuffer);
        } else if (type == (byte) MessageType.GOOD_BYE.ordinal()) {
            fsm.getTransitions().goodBye();
        } else {
            LOG.error("Message type should be MSG or GOOD_BYE but is {} instead",
                      MessageType.values()[type]);
            fsm.getTransitions().protocolError();
            return;
        }

    }

    public void send(String msg) {
        try {
            sendGate.await();
            fsm.getTransitions().transmitMessage(msg);
        } catch (InterruptedException e) {
            return;
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.hellblazer.pinkie.buffer.fsmExample.SimpleProtocol#sendGoodbye()
     */
    @Override
    public void sendGoodbye() {
        ByteBuffer buffer = bufferProtocol.getWriteBuffer();
        buffer.put((byte) MessageType.GOOD_BYE.ordinal());
        buffer.flip();

        bufferProtocol.selectForWrite();
    }

    @Override
    public void setBufferProtocol(BufferProtocol bp, String fsmName) {
        bufferProtocol = bp;
        fsm.setName(fsmName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.hellblazer.pinkie.buffer.fsmExample.SimpleProtocol#sendMessage(com
     * .hellblazer.pinkie.buffer.fsmExample.Message)
     */
    @Override
    public void transmitMessage(String message) {
        ByteBuffer buffer = bufferProtocol.getWriteBuffer();
        buffer.rewind();
        buffer.limit(message.length() + 2);
        buffer.put((byte) MessageType.MSG.ordinal());
        bufferProtocol.setReadFullBuffer(false);
        buffer.put((byte) message.length());
        buffer.put(message.getBytes());
        buffer.flip();
        bufferProtocol.selectForWrite();

        // clear the buffer of all the old crap
        ByteBuffer readBuffer = bufferProtocol.getReadBuffer();
        readBuffer.limit(258);
        bufferProtocol.selectForRead();

    }

    /**
     * @param readBuffer
     */
    private void readMessage(ByteBuffer readBuffer) {
        if (readBuffer.limit() < 2) {
            readBuffer.reset();
            bufferProtocol.selectForRead();
            return;
        }
        byte size = readBuffer.get(1);

        // if the limit is less than the size, we still have
        // to wait for more of the message
        if (readBuffer.limit() < size + 2) {
            readBuffer.reset();
            bufferProtocol.selectForRead();
            return;
        }

        byte[] message = new byte[size];
        readBuffer.position(2);
        readBuffer.get(message);

        readBuffer.limit(0);
        readBuffer.rewind();

        String msg = new String(message);
        messageHandler.handle(msg);
        fsm.getTransitions().messageProcessed();
    }

    BufferProtocolHandler getBufferProtocolHandler() {
        return handler;
    }

}
