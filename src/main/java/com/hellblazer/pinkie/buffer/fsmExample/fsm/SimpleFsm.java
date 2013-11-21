package com.hellblazer.pinkie.buffer.fsmExample.fsm;

import com.hellblazer.pinkie.buffer.BufferProtocol;

public interface SimpleFsm {

    SimpleFsm accepted(BufferProtocol bufferProtocol);

    SimpleFsm ackReceived();

    SimpleFsm close();

    SimpleFsm closing();

    SimpleFsm connected(BufferProtocol bufferProtocol);

    SimpleFsm goodBye();

    SimpleFsm messageProcessed();

    SimpleFsm protocolError();

    SimpleFsm readError();

    SimpleFsm readReady();

    SimpleFsm transmitMessage(String message);

    SimpleFsm writeError();

    SimpleFsm writeReady();
}
