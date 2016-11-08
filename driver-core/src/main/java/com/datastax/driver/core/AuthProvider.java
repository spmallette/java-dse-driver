/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.datastax.driver.core.exceptions.AuthenticationException;

import java.net.InetSocketAddress;

/**
 * Provides {@link Authenticator} instances for use when connecting
 * to Cassandra nodes.
 * <p/>
 * See {@link PlainTextAuthProvider} for an implementation which uses SASL
 * PLAIN mechanism to authenticate using username/password strings
 */
public interface AuthProvider {

    /**
     * A provider that provides no authentication capability.
     * <p/>
     * This is only useful as a placeholder when no authentication is to be used.
     */
    public static final AuthProvider NONE = new AuthProvider() {
        @Override
        public Authenticator newAuthenticator(InetSocketAddress host, String authenticator) {
            throw new AuthenticationException(host,
                    String.format("Host %s requires authentication, but no authenticator found in Cluster configuration", host));
        }
    };

    /**
     * The {@code Authenticator} to use when connecting to {@code host}
     *
     * @param host          the Cassandra host to connect to.
     * @param authenticator the configured authenticator on the host.
     * @return The authentication implementation to use.
     */
    public Authenticator newAuthenticator(InetSocketAddress host, String authenticator) throws AuthenticationException;
}
