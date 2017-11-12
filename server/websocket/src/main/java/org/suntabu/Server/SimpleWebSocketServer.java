package org.suntabu.Server;

/*
 * #%L
 * NanoHttpd-Webserver
 * %%
 * Copyright (C) 2012 - 2017 nanohttpd
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the nanohttpd nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.websockets.*;
import org.suntabu.commands.Console;
import org.suntabu.websockets.WebsocketListener;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleWebSocketServer extends NanoWSD {




    /**
     * logger to log to.
     */
    private static final Logger LOG = Logger.getLogger(SimpleWebSocket.class.getName());

    private final boolean debug;

    private final WebsocketListener mWebSocketListenr;

    private SimpleWebSocket mWebSocket;

    public SimpleWebSocketServer(int port, WebsocketListener websocketListener) {
        super(port);
        debug = true;
        mWebSocketListenr = websocketListener;
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        mWebSocket = new SimpleWebSocket(this, handshake);
        return mWebSocket;
    }

    public void send(String msg) {
        if (mWebSocket != null && isAlive()) {
            try {
                mWebSocket.send(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("send failed");
        }
    }

    private static class SimpleWebSocket extends WebSocket {

        private final SimpleWebSocketServer server;

        public SimpleWebSocket(SimpleWebSocketServer server, IHTTPSession handshakeRequest) {
            super(handshakeRequest);
            this.server = server;
        }

        @Override
        protected void onOpen() {
            WebSocketFrame wsf = null;
            server.mWebSocketListenr.onOpen();
        }

        @Override
        protected void onClose(CloseCode code, String reason, boolean initiatedByRemote) {
            if (server.debug) {
                System.out.println("C [" + (initiatedByRemote ? "Remote" : "Self") + "] " + (code != null ? code : "UnknownCloseCode[" + code + "]")
                        + (reason != null && !reason.isEmpty() ? ": " + reason : ""));
            }

            server.mWebSocketListenr.onClose();
        }

        @Override
        protected void onMessage(WebSocketFrame message) {
            server.mWebSocketListenr.onMessage(message, server);
        }

        @Override
        protected void onPong(WebSocketFrame pong) {
            if (server.debug) {
                System.out.println("P " + pong);
            }
        }

        @Override
        protected void onException(IOException exception) {
            SimpleWebSocketServer.LOG.log(Level.SEVERE, "exception occured", exception);
        }

        @Override
        protected void debugFrameReceived(WebSocketFrame frame) {
            if (server.debug) {
                System.out.println("R " + frame);
            }
        }

        @Override
        protected void debugFrameSent(WebSocketFrame frame) {
            if (server.debug) {
                System.out.println("S " + frame);
            }
        }
    }
}
