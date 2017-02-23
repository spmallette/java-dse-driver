/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.auth;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.Authenticator;
import com.datastax.driver.core.exceptions.AuthenticationException;
import com.google.common.base.Charsets;
import com.google.common.primitives.Bytes;

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
    private final String authorizationId;

    /**
     * Creates an {@link AuthProvider} for the given username and password.
     *
     * @param username The username; cannot be {@code null}.
     * @param password The password; cannot be {@code null}.
     */
    public DsePlainTextAuthProvider(String username, String password) {
        checkNotNull(username, "username cannot be null");
        checkNotNull(password, "password cannot be null");
        this.username = username;
        this.password = password;
        this.authorizationId = "";
    }

    /**
     * Creates an {@link AuthProvider} for the given authentication ID (username),
     * password and authorization ID (authorizationId).
     * </p>
     * Providing an authorization ID allows the currently authenticated user
     * to act as a different user (a.k.a. proxy authentication).
     *
     * @param username        The username; cannot be {@code null}.
     * @param password        The password; cannot be {@code null}.
     * @param authorizationId The authorization ID; cannot be {@code null}.
     */
    public DsePlainTextAuthProvider(String username, String password, String authorizationId) {
        checkNotNull(username, "username cannot be null");
        checkNotNull(password, "password cannot be null");
        checkNotNull(authorizationId, "authorizationId cannot be null");
        this.username = username;
        this.password = password;
        this.authorizationId = authorizationId;
    }

    @Override
    public Authenticator newAuthenticator(InetSocketAddress host, String authenticator) throws AuthenticationException {
        return new PlainTextAuthenticator(authenticator, username, password, authorizationId);
    }

    private static class PlainTextAuthenticator extends BaseDseAuthenticator {

        private static final byte[] MECHANISM = "PLAIN".getBytes(Charsets.UTF_8);
        private static final byte[] SERVER_INITIAL_CHALLENGE = "PLAIN-START".getBytes(Charsets.UTF_8);
        private static final byte[] NULL = new byte[]{0};

        private final byte[] authenticationId;
        private final byte[] password;
        private final byte[] authorizationId;

        PlainTextAuthenticator(String authenticator, String authenticationId, String password, String authorizationId) {
            super(authenticator);
            this.authenticationId = authenticationId.getBytes(Charsets.UTF_8);
            this.password = password.getBytes(Charsets.UTF_8);
            this.authorizationId = authorizationId.getBytes(Charsets.UTF_8);
        }

        @Override
        public byte[] getMechanism() {
            return MECHANISM.clone();
        }

        @Override
        public byte[] getInitialServerChallenge() {
            return SERVER_INITIAL_CHALLENGE.clone();
        }

        @Override
        public byte[] evaluateChallenge(byte[] challenge) {
            if (Arrays.equals(SERVER_INITIAL_CHALLENGE, challenge)) {
                // The SASL plain text format is authorizationId NUL username NUL password
                return Bytes.concat(authorizationId, NULL, authenticationId, NULL, password);
            }
            throw new RuntimeException("Incorrect challenge from server");
        }
    }
}
