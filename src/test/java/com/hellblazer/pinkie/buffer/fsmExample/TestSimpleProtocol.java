package com.hellblazer.pinkie.buffer.fsmExample;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.hellblazer.pinkie.CommunicationsHandler;
import com.hellblazer.pinkie.SocketChannelHandler;
import com.hellblazer.pinkie.buffer.BufferProtocol;
import com.hellblazer.pinkie.buffer.BufferProtocolHandler;
import com.hellblazer.pinkie.buffer.fsmExample.SimpleProtocol.MessageType;

public class TestSimpleProtocol {

	@Test
	public void testEstablish() {
		SimpleProtocolImpl protocol = new SimpleProtocolImpl();
		BufferProtocolHandler handler = protocol.getBufferProtocolHandler();
		BufferProtocol bufferProtocol = new BufferProtocol(protocol.getBufferProtocolHandler());
		CommunicationsHandler commHandler = bufferProtocol.getHandler();
		
		SocketChannelHandler socketHandler = mock(SocketChannelHandler.class);
		assertEquals(SimpleProtocolContext.Simple.Initial, protocol.getCurrentState());
		
		commHandler.accept(socketHandler);
		assertEquals(SimpleProtocolContext.SimpleClient.Connect, protocol.getCurrentState());
		
		verify(socketHandler).selectForWrite();
		verify(socketHandler).selectForRead();
		
		bufferProtocol.getReadBuffer().put((byte)MessageType.ACK.ordinal());
		handler.readReady();
		assertEquals(SimpleProtocolContext.SimpleClient.SendMessage, protocol.getCurrentState());
	}
}
