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
import com.hellblazer.pinkie.buffer.fsmExample.SimpleProtocolContext.SimpleProtocolState;
import com.hellblazer.utils.Gate;

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
		public void readReady() {
			fsm.readReady();
		}

		@Override
		public void writeError() {
			fsm.writeError();
		}

		@Override
		public void writeReady() {
			fsm.writeReady();
		}
	}

	private final BufferProtocolHandlerImpl handler;
	private final SimpleProtocolContext fsm;

	private BufferProtocol bufferProtocol;

	private Gate sendGate;

	public SimpleProtocolImpl() {
		fsm = new SimpleProtocolContext(this);
		handler = new BufferProtocolHandlerImpl();
		sendGate = new Gate();
	}

	public void send(String msg) {
		try {
			sendGate.await();
			fsm.transmitMessage(msg);
		} catch (InterruptedException e) {
			return;
		}

	}

	@Override
	public void setBufferProtocol(BufferProtocol bp) {
		bufferProtocol = bp;
	}

	public BufferProtocolHandler getBufferProtocolHandler() {
		return handler;
	}

	@Override
	public void establishClientSession() {
		ByteBuffer buffer = bufferProtocol.getWriteBuffer();
		buffer.put((byte) MessageType.ESTABLISH.ordinal());
		buffer.flip();
		ByteBuffer readBuffer = bufferProtocol.getReadBuffer();
		readBuffer.limit(1);
		readBuffer.rewind();
		bufferProtocol.selectForWrite();
		// waiting for stuff to read...
		bufferProtocol.selectForRead();
	}

	@Override
	public void ackReceived() {
		ByteBuffer readBuffer = bufferProtocol.getReadBuffer();
		readBuffer.rewind();
		byte type = readBuffer.get();
		readBuffer.limit(0);
		readBuffer.rewind();
		if (type == (byte) MessageType.ACK.ordinal()) {
			fsm.ackReceived();
		} else {
			fsm.protocolError();
		}

	}

	/**
	 * @return
	 */
	public SimpleProtocolState getCurrentState() {
		return fsm.getState();
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

	public void close() {
		fsm.close();
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
		readBuffer.limit(0);
		readBuffer.rewind();
		if (type != (byte) MessageType.ESTABLISH.ordinal()) {
			fsm.protocolError();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hellblazer.pinkie.buffer.fsmExample.SimpleProtocol#processMessage()
	 */
	@Override
	public void processMessage() {
		ByteBuffer readBuffer = bufferProtocol.getReadBuffer();
		readBuffer.rewind();
		byte type = readBuffer.get();

		if (type != (byte) MessageType.MSG.ordinal()) {
			fsm.protocolError();
			return;
		}

		byte size = readBuffer.get();
		byte[] message = new byte[size];
		readBuffer.get(message);

		readBuffer.limit(0);
		readBuffer.rewind();

		String msg = new String(message);
		System.out.println(msg);
		fsm.messageProcessed();

	}

	/* (non-Javadoc)
	 * @see com.hellblazer.pinkie.buffer.fsmExample.SimpleProtocol#awaitMessage()
	 */
	@Override
	public void awaitMessage() {
		
		
	}

}
