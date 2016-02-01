/*
 *      Copyright (C) 2012-2015 DataStax Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.datastax.driver;

import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.AuthenticationException;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.policies.*;
import com.datastax.driver.graph.GraphOptions;
import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Extension of the {@link Cluster} object with DSE-specific features.
 */
public class DseCluster extends DelegatingCluster {

    private final Cluster delegate;

    /**
     * Helper class to build {@link DseCluster} instances.
     */
    public static class Builder extends Cluster.Builder {

        private GraphOptions graphOptions;

        public DseCluster.Builder withGraphOptions(GraphOptions graphOptions) {
            this.graphOptions = graphOptions;
            return this;
        }

        @Override
        public DseCluster.Builder withClusterName(String name) {
            return (DseCluster.Builder) super.withClusterName(name);
        }

        @Override
        public DseCluster.Builder withPort(int port) {
            return (DseCluster.Builder) super.withPort(port);
        }

        @Override
        public DseCluster.Builder withMaxSchemaAgreementWaitSeconds(int maxSchemaAgreementWaitSeconds) {
            return (DseCluster.Builder) super.withMaxSchemaAgreementWaitSeconds(maxSchemaAgreementWaitSeconds);
        }

        @Override
        public DseCluster.Builder withProtocolVersion(ProtocolVersion version) {
            return (DseCluster.Builder) super.withProtocolVersion(version);
        }

        @Override
        public DseCluster.Builder addContactPoint(String address) {
            return (Builder) super.addContactPoint(address);
        }

        @Override
        public DseCluster.Builder addContactPoints(String... addresses) {
            return (Builder) super.addContactPoints(addresses);
        }

        @Override
        public DseCluster.Builder addContactPoints(InetAddress... addresses) {
            return (Builder) super.addContactPoints(addresses);
        }

        @Override
        public DseCluster.Builder addContactPoints(Collection<InetAddress> addresses) {
            return (Builder) super.addContactPoints(addresses);
        }

        @Override
        public DseCluster.Builder addContactPointsWithPorts(Collection<InetSocketAddress> addresses) {
            return (Builder) super.addContactPointsWithPorts(addresses);
        }

        @Override
        public DseCluster.Builder withLoadBalancingPolicy(LoadBalancingPolicy policy) {
            return (DseCluster.Builder) super.withLoadBalancingPolicy(policy);
        }

        @Override
        public DseCluster.Builder withReconnectionPolicy(ReconnectionPolicy policy) {
            return (DseCluster.Builder) super.withReconnectionPolicy(policy);
        }

        @Override
        public DseCluster.Builder withRetryPolicy(RetryPolicy policy) {
            return (DseCluster.Builder) super.withRetryPolicy(policy);
        }

        @Override
        public DseCluster.Builder withAddressTranslator(AddressTranslator translator) {
            return (DseCluster.Builder) super.withAddressTranslator(translator);
        }

        @Override
        public DseCluster.Builder withTimestampGenerator(TimestampGenerator timestampGenerator) {
            return (DseCluster.Builder) super.withTimestampGenerator(timestampGenerator);
        }

        @Override
        public DseCluster.Builder withSpeculativeExecutionPolicy(SpeculativeExecutionPolicy policy) {
            return (DseCluster.Builder) super.withSpeculativeExecutionPolicy(policy);
        }

        @Override
        public DseCluster.Builder withCodecRegistry(CodecRegistry codecRegistry) {
            return (DseCluster.Builder) super.withCodecRegistry(codecRegistry);
        }

        @Override
        public DseCluster.Builder withCredentials(String username, String password) {
            return (DseCluster.Builder) super.withCredentials(username, password);
        }

        @Override
        public DseCluster.Builder withAuthProvider(AuthProvider authProvider) {
            return (DseCluster.Builder) super.withAuthProvider(authProvider);
        }

        @Override
        public DseCluster.Builder withCompression(ProtocolOptions.Compression compression) {
            return (DseCluster.Builder) super.withCompression(compression);
        }

        @Override
        public DseCluster.Builder withoutMetrics() {
            return (DseCluster.Builder) super.withoutMetrics();
        }

        @Override
        public DseCluster.Builder withSSL() {
            return (DseCluster.Builder) super.withSSL();
        }

        @Override
        public DseCluster.Builder withSSL(SSLOptions sslOptions) {
            return (DseCluster.Builder) super.withSSL(sslOptions);
        }

        @Override
        public DseCluster.Builder withInitialListeners(Collection<Host.StateListener> listeners) {
            return (DseCluster.Builder) super.withInitialListeners(listeners);
        }

        @Override
        public DseCluster.Builder withoutJMXReporting() {
            return (DseCluster.Builder) super.withoutJMXReporting();
        }

        @Override
        public DseCluster.Builder withPoolingOptions(PoolingOptions options) {
            return (DseCluster.Builder) super.withPoolingOptions(options);
        }

        @Override
        public DseCluster.Builder withSocketOptions(SocketOptions options) {
            return (DseCluster.Builder) super.withSocketOptions(options);
        }

        @Override
        public DseCluster.Builder withQueryOptions(QueryOptions options) {
            return (DseCluster.Builder) super.withQueryOptions(options);
        }

        @Override
        public DseCluster.Builder withNettyOptions(NettyOptions nettyOptions) {
            return (DseCluster.Builder) super.withNettyOptions(nettyOptions);
        }

        @Override
        public DseCluster build() {
            return new DseCluster(super.build());
        }

        @Override
        public DseConfiguration getConfiguration() {
            return new DseConfiguration(super.getConfiguration(), graphOptions != null ? graphOptions : new GraphOptions());
        }
    }

    /**
     * Creates a new {@link DseCluster.Builder} instance.
     * <p/>
     * This is a convenience method for {@code new GraphCluster.Builder()}.
     *
     * @return the new cluster builder.
     */
    public static DseCluster.Builder builder() {
        return new DseCluster.Builder();
    }

    private DseCluster(Cluster delegate) {
        checkArgument(delegate.getConfiguration() instanceof DseConfiguration, "Cannot create a GraphCluster without a GraphConfiguration");
        this.delegate = delegate;
    }

    @Override
    public DseConfiguration getConfiguration() {
        return (DseConfiguration) super.getConfiguration();
    }

    @Override
    protected Cluster delegate() {
        return delegate;
    }

    /**
     * Creates a new DSE session on this cluster but does not initialize it.
     * <p>
     * Because this method does not perform any initialization, it cannot fail.
     * The initialization of the session (the connection of the DseSession to the
     * Cassandra nodes) will occur if either the {@link DseSession#init} method is
     * called explicitly, or whenever the returned session object is used.
     * <p>
     * Once a session returned by this method gets initialized (see above), it
     * will be set to no keyspace. If you want to set such session to a
     * keyspace, you will have to explicitly execute a 'USE mykeyspace' query.
     * <p>
     * Note that if you do not particularly need to defer initialization, it is
     * simpler to use one of the {@code connect()} methods of this class.
     *
     * @return a new, non-initialized Graph session on this cluster.
     */
    /**
     * {@inheritDoc}
     */
    @Override
    public DseSession newSession() {
        return new DefaultDseSession(super.newSession(), getConfiguration().getGraphOptions());
    }

    /**
     * Creates a new DSE session on this cluster and initializes it.
     * <p/>
     * Note that this method will initialize the newly created DSE session, trying
     * to connect to the Cassandra nodes before returning. If you only want to
     * create a DseSession object without initializing it right away, see
     * {@link #newSession}.
     *
     * @return a new session on this cluster sets to no keyspace.
     * @throws NoHostAvailableException if the Cluster has not been initialized
     *                                  yet ({@link #init} has not be called and this is the first connect call)
     *                                  and no host amongst the contact points can be reached.
     * @throws AuthenticationException  if an authentication error occurs while
     *                                  contacting the initial contact points.
     * @throws IllegalStateException    if the Cluster was closed prior to calling
     *                                  this method. This can occur either directly (through {@link #close()} or
     *                                  {@link #closeAsync()}), or as a result of an error while initializing the
     *                                  Cluster.
     */
    @Override
    public DseSession connect() {
        return new DefaultDseSession(super.connect(), getConfiguration().getGraphOptions());
    }

    /**
     * Creates a new DSE session on this cluster, initialize it and sets the
     * keyspace to the provided one.
     * <p/>
     * Note that this method will initialize the newly created DSE session, trying
     * to connect to the Cassandra nodes before returning. If you only want to
     * create a DseSession object without initializing it right away, see
     * {@link #newSession}.
     * <p/>
     * Note that the keyspace set through this method is only valid
     * for CQL queries; Graph namespaces (that map to keyspaces in Cassandra)
     * should be selected via the {@link GraphOptions#setGraphName(String)}
     * method.
     *
     * @param keyspace The name of the keyspace to use for the created
     *                 {@link DseSession}.
     * @return a new session on this cluster sets to keyspace
     * {@code keyspaceName}.
     * @throws NoHostAvailableException if the Cluster has not been initialized
     *                                  yet ({@link #init} has not be called and this is the first connect call)
     *                                  and no host amongst the contact points can be reached, or if no host can
     *                                  be contacted to set the {@code keyspace}.
     * @throws AuthenticationException  if an authentication error occurs while
     *                                  contacting the initial contact points.
     * @throws InvalidQueryException    if the keyspace does not exist.
     * @throws IllegalStateException    if the Cluster was closed prior to calling
     *                                  this method. This can occur either directly (through {@link #close()} or
     *                                  {@link #closeAsync()}), or as a result of an error while initializing the
     *                                  Cluster.
     */
    @Override
    public DseSession connect(String keyspace) {
        return new DefaultDseSession(super.connect(keyspace), getConfiguration().getGraphOptions());
    }

    /**
     * Creates a new DSE session on this cluster and initializes it asynchronously.
     * <p/>
     * This will also initialize the {@code Cluster} if needed; note that cluster
     * initialization happens synchronously on the thread that called this method.
     * Therefore it is recommended to initialize the cluster at application
     * startup, and not rely on this method to do it.
     * <p/>
     * The {@link Session} object returned by the future's {@link Future#get() get} method can be safely cast
     * to {@link DseSession}.
     *
     * @return a future that will complete when the session is fully initialized.
     * @throws NoHostAvailableException if the Cluster has not been initialized
     *                                  yet ({@link #init} has not been called and this is the first connect call)
     *                                  and no host amongst the contact points can be reached.
     * @throws IllegalStateException    if the Cluster was closed prior to calling
     *                                  this method. This can occur either directly (through {@link #close()} or
     *                                  {@link #closeAsync()}), or as a result of an error while initializing the
     *                                  Cluster.
     * @see #connect()
     */
    @Override
    public ListenableFuture<Session> connectAsync() {
        return Futures.transform(super.connectAsync(), new Function<Session, Session>() {
            @Override
            public Session apply(Session input) {
                return new DefaultDseSession(input, getConfiguration().getGraphOptions());
            }
        });
    }

    /**
     * Creates a new DSE session on this cluster, and initializes it to the given
     * keyspace asynchronously.
     * <p/>
     * This will also initialize the {@code Cluster} if needed; note that cluster
     * initialization happens synchronously on the thread that called this method.
     * Therefore it is recommended to initialize the cluster at application
     * startup, and not rely on this method to do it.
     * <p/>
     * Note that the keyspace set through this method is only valid
     * for CQL queries; Graph namespaces (that map to keyspaces in Cassandra)
     * should be selected via the {@link GraphOptions#setGraphName(String)}
     * method.
     * <p/>
     * The {@link Session} object returned by the future's {@link Future#get() get} method can be safely cast
     * to {@link DseSession}.
     *
     * @param keyspace The name of the keyspace to use for the created
     *                 {@code Session}.
     * @return a future that will complete when the session is fully initialized.
     * @throws NoHostAvailableException if the Cluster has not been initialized
     *                                  yet ({@link #init} has not been called and this is the first connect call)
     *                                  and no host amongst the contact points can be reached.
     * @throws IllegalStateException    if the Cluster was closed prior to calling
     *                                  this method. This can occur either directly (through {@link #close()} or
     *                                  {@link #closeAsync()}), or as a result of an error while initializing the
     *                                  Cluster.
     */
    @Override
    public ListenableFuture<Session> connectAsync(String keyspace) {
        return Futures.transform(super.connectAsync(keyspace), new Function<Session, Session>() {
            @Override
            public Session apply(Session input) {
                return new DefaultDseSession(input, getConfiguration().getGraphOptions());
            }
        });
    }
}
