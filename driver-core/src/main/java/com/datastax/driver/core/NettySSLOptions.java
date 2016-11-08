/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

/**
 * {@link SSLOptions} implementation based on Netty's SSL context.
 * <p/>
 * Netty has the ability to use OpenSSL if available, instead of the JDK's built-in engine. This yields better performance.
 */
public class NettySSLOptions implements SSLOptions {
    private final SslContext context;

    /**
     * Create a new instance from a given context.
     *
     * @param context the Netty context. {@code SslContextBuilder.forClient()} provides a fluent API to build it.
     */
    public NettySSLOptions(SslContext context) {
        this.context = context;
    }

    @Override
    public SslHandler newSSLHandler(SocketChannel channel) {
        return context.newHandler(channel.alloc());
    }
}
