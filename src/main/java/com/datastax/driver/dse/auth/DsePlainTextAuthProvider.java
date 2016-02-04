/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.auth;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.Authenticator;
import com.datastax.driver.core.exceptions.AuthenticationException;
import com.google.common.base.Charsets;

import java.net.InetSocketAddress;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * AuthProvider that provides plain text authenticator instances for clients to connect
 * to DSE clusters secured with the DseAuthenticator.
 * <p/>
 * To create a cluster using this auth provider:
 * <pre>
 * Cluster cluster = Cluster.builder()
 *                          .addContactPoint(hostname)
 *                          .withAuthProvider(new DsePlainTextAuthProvider("username", "password"))
 *                          .build();
 * </pre>
 */
public class DsePlainTextAuthProvider implements AuthProvider {
    private final String username;
    private final String password;

    public DsePlainTextAuthProvider(String username, String password) {
        checkNotNull(username, "username cannot be null");
        checkNotNull(password, "password cannot be null");
        this.username = username;
        this.password = password;
    }

    public Authenticator newAuthenticator(InetSocketAddress host, String authenticator) throws AuthenticationException {
        return new PlainTextAuthenticator(authenticator, username, password);
    }

    private static class PlainTextAuthenticator extends BaseDseAuthenticator {
        private static final byte[] MECHANISM = "PLAIN".getBytes(Charsets.UTF_8);
        private static final byte[] SERVER_INITIAL_CHALLENGE = "PLAIN-START".getBytes(Charsets.UTF_8);
        private final byte[] username;
        private final byte[] password;

        public PlainTextAuthenticator(String authenticator, String username, String password) {
            super(authenticator);
            this.username = username.getBytes(Charsets.UTF_8);
            this.password = password.getBytes(Charsets.UTF_8);
        }

        public byte[] getMechanism() {
            return MECHANISM.clone();
        }

        public byte[] getInitialServerChallenge() {
            return SERVER_INITIAL_CHALLENGE.clone();
        }

        public byte[] evaluateChallenge(byte[] challenge) {
            if (Arrays.equals(SERVER_INITIAL_CHALLENGE, challenge)) {
                byte[] token = new byte[username.length + password.length + 2];
                token[0] = 0;
                System.arraycopy(username, 0, token, 1, username.length);
                token[username.length + 1] = 0;
                System.arraycopy(password, 0, token, username.length + 2, password.length);
                return token;
            }
            throw new RuntimeException("Incorrect challenge from server");
        }
    }
}
