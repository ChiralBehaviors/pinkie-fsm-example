/**
 * Copyright (c) 2012, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.hellblazer.pinkie.buffer.fsmExample;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Test;

import statemap.StateUndefinedException;

import com.hellblazer.pinkie.ChannelHandler;
import com.hellblazer.pinkie.ServerSocketChannelHandler;
import com.hellblazer.pinkie.SocketOptions;
import com.hellblazer.pinkie.buffer.BufferProtocol;
import com.hellblazer.utils.Condition;
import com.hellblazer.utils.Utils;

import static org.junit.Assert.*;

/**
 * @author hparry
 * 
 */
public class TestClientServer {

	private ChannelHandler clientHandler;
	private ServerSocketChannelHandler serverHandler;

	@Test
	public void testClientServer() throws IOException {

		int bufferSize = 12;
		SocketOptions socketOptions = new SocketOptions();
		socketOptions.setSend_buffer_size(bufferSize);
		socketOptions.setReceive_buffer_size(bufferSize);
		socketOptions.setTimeout(100);

		constructClientHandler(socketOptions);
		constructServerHandler(socketOptions);
		serverHandler.start();
		clientHandler.start();

		final SimpleProtocolImpl client = new SimpleProtocolImpl();
		BufferProtocol clientProtocol = new BufferProtocol(
				client.getBufferProtocolHandler());
		clientHandler.connectTo(serverHandler.getLocalAddress(),
				clientProtocol.getHandler());
		
		assertTrue(Utils.waitForCondition(1000, new Condition() {
			
			@Override
			public boolean isTrue() {
				try {
					return client.getCurrentState().equals(SimpleProtocolContext.SimpleClient.SendMessage);
				} catch (StateUndefinedException e) {
					return false;
				}
			}
		}));
	}

	private void constructClientHandler(SocketOptions socketOptions)
			throws IOException {
		clientHandler = new ChannelHandler("Client", socketOptions,
				Executors.newCachedThreadPool());
	}

	private void constructServerHandler(SocketOptions socketOptions)
			throws IOException {
		serverHandler = new ServerSocketChannelHandler("Server", socketOptions,
				new InetSocketAddress("127.0.0.1", 0),
				Executors.newCachedThreadPool(), new SimpleProtocolFactory());
	}

	@After
	public void cleanUp() {
		if (clientHandler != null) {
			clientHandler.terminate();
		}
		if (serverHandler != null) {
			serverHandler.terminate();
		}
	}

}
