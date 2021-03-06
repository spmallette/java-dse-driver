/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.security.NoSuchAlgorithmException;

/**
 * {@link SSLOptions} implementation based on built-in JDK classes.
 *
 * @deprecated Use {@link RemoteEndpointAwareJdkSSLOptions} instead.
 */
@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated
public class JdkSSLOptions implements SSLOptions {

    /**
     * Creates a builder to create a new instance.
     *
     * @return the builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    protected final SSLContext context;
    protected final String[] cipherSuites;

    /**
     * Creates a new instance.
     *
     * @param context      the SSL context.
     * @param cipherSuites the cipher suites to use.
     */
    protected JdkSSLOptions(SSLContext context, String[] cipherSuites) {
        this.context = (context == null) ? makeDefaultContext() : context;
        this.cipherSuites = cipherSuites;
    }

    @Override
    public SslHandler newSSLHandler(SocketChannel channel) {
        SSLEngine engine = newSSLEngine(channel);
        return new SslHandler(engine);
    }

    /**
     * Creates an SSL engine each time a connection is established.
     * <p/>
     * <p/>
     * You might want to override this if you need to fine-tune the engine's configuration
     * (for example enabling hostname verification).
     *
     * @param channel the Netty channel for that connection.
     * @return the engine.
     */
    protected SSLEngine newSSLEngine(@SuppressWarnings("unused") SocketChannel channel) {
        SSLEngine engine = context.createSSLEngine();
        engine.setUseClientMode(true);
        if (cipherSuites != null)
            engine.setEnabledCipherSuites(cipherSuites);
        return engine;
    }

    private static SSLContext makeDefaultContext() throws IllegalStateException {
        try {
            return SSLContext.getDefault();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Cannot initialize SSL Context", e);
        }
    }

    /**
     * Helper class to build JDK-based SSL options.
     */
    public static class Builder {
        protected SSLContext context;
        protected String[] cipherSuites;

        /**
         * Set the SSL context to use.
         * <p/>
         * If this method isn't called, a context with the default options will be used,
         * and you can use the default
         * <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/security/jsse/JSSERefGuide.html#Customization">JSSE System properties</a>
         * to customize its behavior. This may in particular involve
         * <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/security/jsse/JSSERefGuide.html#CreateKeystore">creating a simple keyStore and trustStore</a>.
         *
         * @param context the SSL context.
         * @return this builder.
         */
        public Builder withSSLContext(SSLContext context) {
            this.context = context;
            return this;
        }

        /**
         * Set the cipher suites to use.
         * <p/>
         * If this method isn't called, the default is to present all the eligible client ciphers to the server.
         *
         * @param cipherSuites the cipher suites to use.
         * @return this builder.
         */
        public Builder withCipherSuites(String[] cipherSuites) {
            this.cipherSuites = cipherSuites;
            return this;
        }

        /**
         * Builds a new instance based on the parameters provided to this builder.
         *
         * @return the new instance.
         */
        @SuppressWarnings("deprecation")
        public JdkSSLOptions build() {
            return new JdkSSLOptions(context, cipherSuites);
        }
    }
}
