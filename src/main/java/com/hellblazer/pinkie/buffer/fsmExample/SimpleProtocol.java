package com.hellblazer.pinkie.buffer.fsmExample;

import com.hellblazer.pinkie.buffer.BufferProtocol;
import com.hellblazer.pinkie.buffer.BufferProtocolHandler;

/**
 * 
 * @author hhildebrand
 * 
 */
public interface SimpleProtocol extends BufferProtocolHandler {
    public enum MessageType {
        ESTABLISH,
        HELLO,
        GOOD_BYE;
    }

    void setBufferProtocol(BufferProtocol bufferProtocol);
}
