/**
 * Copyright © 2016-2025 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.lwm2m.demo.client;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.scandium.dtls.ClientHandshaker;
import org.eclipse.californium.scandium.dtls.DTLSContext;
import org.eclipse.californium.scandium.dtls.HandshakeException;
import org.eclipse.californium.scandium.dtls.Handshaker;
import org.eclipse.californium.scandium.dtls.ResumingClientHandshaker;
import org.eclipse.californium.scandium.dtls.ResumingServerHandshaker;
import org.eclipse.californium.scandium.dtls.ServerHandshaker;
import org.eclipse.californium.scandium.dtls.SessionAdapter;
import org.eclipse.californium.scandium.dtls.SessionId;

@Slf4j
public class DtlsSessionLogger extends SessionAdapter {

    private SessionId sessionIdentifier = null;

    @Override
    public void handshakeStarted(Handshaker handshaker) throws HandshakeException {
        if (handshaker instanceof ResumingServerHandshaker) {
            log.info("DTLS abbreviated Handshake initiated by server : STARTED ...");
        } else if (handshaker instanceof ServerHandshaker) {
            log.info("DTLS Full Handshake initiated by server : STARTED ...");
        } else if (handshaker instanceof ResumingClientHandshaker) {
            sessionIdentifier = handshaker.getSession().getSessionIdentifier();
            log.info("DTLS abbreviated Handshake initiated by client : STARTED ...");
        } else if (handshaker instanceof ClientHandshaker) {
            log.info("DTLS Full Handshake initiated by client : STARTED ...");
        }
    }

    @Override
    public void contextEstablished(Handshaker handshaker, DTLSContext establishedContext) throws HandshakeException {
        if (handshaker instanceof ResumingServerHandshaker) {
            log.info("DTLS abbreviated Handshake initiated by server : SUCCEED");
        } else if (handshaker instanceof ServerHandshaker) {
            log.info("DTLS Full Handshake initiated by server : SUCCEED");
        } else if (handshaker instanceof ResumingClientHandshaker) {
            if (sessionIdentifier != null && sessionIdentifier.equals(handshaker.getSession().getSessionIdentifier())) {
                log.info("DTLS abbreviated Handshake initiated by client : SUCCEED");
            } else {
                log.info("DTLS abbreviated turns into Full Handshake initiated by client : SUCCEED");
            }
        } else if (handshaker instanceof ClientHandshaker) {
            log.info("DTLS Full Handshake initiated by client : SUCCEED");
        }
    }

    @Override
    public void handshakeFailed(Handshaker handshaker, Throwable error) {
        // get cause
        String cause;
        if (error != null) {
            if (error.getMessage() != null) {
                cause = error.getMessage();
            } else {
                cause = error.getClass().getName();
            }
        } else {
            cause = "unknown cause";
        }

        if (handshaker instanceof ResumingServerHandshaker) {
            log.info("DTLS abbreviated Handshake initiated by server : FAILED ({})", cause);
        } else if (handshaker instanceof ServerHandshaker) {
            log.info("DTLS Full Handshake initiated by server : FAILED ({})", cause);
        } else if (handshaker instanceof ResumingClientHandshaker) {
            log.info("DTLS abbreviated Handshake initiated by client : FAILED ({})", cause);
        } else if (handshaker instanceof ClientHandshaker) {
            log.info("DTLS Full Handshake initiated by client : FAILED ({})", cause);
        }
    }
}
