package com.hellblazer.pinkie.buffer.fsmExample;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import org.junit.Test;

import com.hellblazer.pinkie.CommunicationsHandler;
import com.hellblazer.pinkie.SocketChannelHandler;
import com.hellblazer.pinkie.buffer.BufferProtocol;
import com.hellblazer.pinkie.buffer.BufferProtocolHandler;
import com.hellblazer.pinkie.buffer.fsmExample.SimpleProtocol.MessageType;

public class TestSimpleProtocol {

	@Test
	public void testEstablish() throws IOException {
		SimpleProtocolImpl protocol = new SimpleProtocolImpl();
		BufferProtocolHandler handler = protocol.getBufferProtocolHandler();
		BufferProtocol bufferProtocol = new BufferProtocol(protocol.getBufferProtocolHandler());
		CommunicationsHandler commHandler = bufferProtocol.getHandler();
		
		SocketChannelHandler socketHandler = mock(SocketChannelHandler.class);
		SocketChannel socketChannel = mock(SocketChannel.class);
		when(socketHandler.getChannel()).thenReturn(socketChannel);
		
		when(socketChannel.getLocalAddress()).thenReturn(new InetSocketAddress(666));
		when(socketChannel.getRemoteAddress()).thenReturn(new InetSocketAddress(668));
		when(socketChannel.isConnected()).thenReturn(true);
		assertEquals(SimpleProtocolContext.Simple.Initial, protocol.getCurrentState());
		
		commHandler.accept(socketHandler);
		assertEquals(SimpleProtocolContext.SimpleClient.Connect, protocol.getCurrentState());
		
		verify(socketHandler).selectForWrite();
		verify(socketHandler).selectForRead();
		
		bufferProtocol.getReadBuffer().put((byte)MessageType.ACK.ordinal());
		handler.readReady();
		assertEquals(SimpleProtocolContext.SimpleClient.SendMessage, protocol.getCurrentState());
		
		bufferProtocol.getReadBuffer().rewind();
		String message = "HELLO CLEVELAND!";
		protocol.send(message);
		assertEquals(SimpleProtocolContext.SimpleClient.SendMessage, protocol.getCurrentState());
		
		bufferProtocol.getReadBuffer().put((byte)MessageType.ACK.ordinal());
		handler.readReady();
		assertEquals(SimpleProtocolContext.SimpleClient.SendMessage, protocol.getCurrentState());
	}
}
