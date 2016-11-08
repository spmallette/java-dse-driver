/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;

/**
 * Defines how the driver configures SSL connections.
 *
 * @see JdkSSLOptions
 * @see NettySSLOptions
 */
public interface SSLOptions {

    /**
     * Creates a new SSL handler for the given Netty channel.
     * <p/>
     * This gets called each time the driver opens a new connection to a Cassandra host. The newly created handler will be added
     * to the channel's pipeline to provide SSL support for the connection.
     * <p/>
     * You don't necessarily need to implement this method directly; see the provided implementations: {@link JdkSSLOptions} and
     * {@link NettySSLOptions}.
     *
     * @param channel the channel.
     * @return the handler.
     */
    SslHandler newSSLHandler(SocketChannel channel);
}
