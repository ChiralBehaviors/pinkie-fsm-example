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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Test;

import statemap.StateUndefinedException;

import com.hellblazer.pinkie.ChannelHandler;
import com.hellblazer.pinkie.ServerSocketChannelHandler;
import com.hellblazer.pinkie.SocketOptions;
import com.hellblazer.pinkie.buffer.BufferProtocol;
import com.hellblazer.pinkie.buffer.fsmExample.SimpleProtocolImpl.MessageHandler;
import com.hellblazer.pinkie.buffer.fsmExample.fsm.SimpleClient;
import com.hellblazer.utils.Condition;
import com.hellblazer.utils.Utils;

/**
 * @author hparry
 * 
 */
public class TestClientServer {

    private ChannelHandler                clientHandler;
    private ServerSocketChannelHandler    serverHandler;
    private final AtomicReference<String> messageReceived = new AtomicReference<String>();

    @After
    public void cleanUp() {
        if (clientHandler != null) {
            clientHandler.terminate();
        }
        if (serverHandler != null) {
            serverHandler.terminate();
        }
    }

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

        final SimpleProtocolImpl client = new SimpleProtocolImpl(null);
        BufferProtocol clientProtocol = new BufferProtocol(
                                                           client.getBufferProtocolHandler());
        clientHandler.connectTo(serverHandler.getLocalAddress(),
                                clientProtocol.getHandler());

        assertTrue(Utils.waitForCondition(1000, new Condition() {

            @Override
            public boolean isTrue() {
                try {
                    return client.getCurrentState().equals(SimpleClient.SendMessage);
                } catch (StateUndefinedException e) {
                    return false;
                }
            }
        }));
        String msg = "God this hurts";
        client.send(msg);
        assertTrue(Utils.waitForCondition(1000, new Condition() {

            @Override
            public boolean isTrue() {

                return messageReceived.get() != null;

            }
        }));
        assertEquals(msg, messageReceived.get());
    }

    private void constructClientHandler(SocketOptions socketOptions)
                                                                    throws IOException {
        clientHandler = new ChannelHandler("Client", socketOptions,
                                           Executors.newCachedThreadPool());
    }

    private void constructServerHandler(SocketOptions socketOptions)
                                                                    throws IOException {
        serverHandler = new ServerSocketChannelHandler(
                                                       "Server",
                                                       socketOptions,
                                                       new InetSocketAddress(
                                                                             "127.0.0.1",
                                                                             0),
                                                       Executors.newCachedThreadPool(),
                                                       new SimpleProtocolFactory(
                                                                                 new MessageHandler() {

                                                                                     @Override
                                                                                     public void handle(String message) {

                                                                                         messageReceived.set(message);
                                                                                     }
                                                                                 }));
    }

}
