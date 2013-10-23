package com.hellblazer.pinkie.buffer.fsmExample;

import java.nio.ByteBuffer;

import com.hellblazer.pinkie.buffer.BufferProtocol;
import com.hellblazer.pinkie.buffer.fsmExample.SimpleProtocol;

/**
 * 
 * @author hhildebrand
 * 
 */
public class SimpleProtocolImpl implements SimpleProtocol {

    private final SimpleProtocolContext fsm;
    @SuppressWarnings("unused")
    private BufferProtocol              bufferProtocol;

    public SimpleProtocolImpl() {
        fsm = new SimpleProtocolContext(this);
    }

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
    public void setBufferProtocol(BufferProtocol bufferProtocol) {
        this.bufferProtocol = bufferProtocol;
    }

    @Override
    public void writeError() {
        fsm.writeError();
    }

    @Override
    public void writeReady(ByteBuffer writeBuffer) {
        fsm.writeReady(writeBuffer);
    }

    @Override
    public void send(String msg) {
        // TODO Auto-generated method stub

    }
}
