/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.examples.dse.auth;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.auth.DsePlainTextAuthProvider;

/**
 * Demonstrates proxy authentication, where the driver can authenticate as a given user, but then act as another user or
 * role once the connection is established.
 * <p>
 * Preconditions:
 * - a DSE cluster is running and accessible through the contacts points identified by CONTACT_POINTS and PORT.
 * - the cluster is configured to use authentication and authorization. On a fresh DSE installation, a basic
 * configuration with internal authentication can be achieved with the following:
 * <pre>
 *     cassandra.yaml:
 *         authenticator: com.datastax.bdp.cassandra.auth.DseAuthenticator
 *         authorizer: com.datastax.bdp.cassandra.auth.DseAuthorizer
 *
 *     dse.yaml:
 *         authentication_options:
 *             enabled: true
 *             default_scheme: internal
 *             other_schemes:
 *             scheme_permissions: false
 *             allow_digest_with_kerberos: true
 *             plain_text_without_ssl: warn
 *             transitional_mode: disabled
 *         authorization_options:
 *             enabled: true
 *             transitional_mode: disabled
 *             allow_row_level_security: false
 * </pre>
 * - there is a user 'alice' with access to the keyspace 'test':
 * <pre>
 *     CREATE KEYSPACE test WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
 *     CREATE TABLE test.foo(i int primary key);
 *     CREATE USER alice WITH PASSWORD 'alice';
 *     GRANT ALL ON KEYSPACE test TO alice;
 * </pre>
 * - there is a user 'bob' with no access to 'test', but it can login as 'alice':
 * <pre>
 *     CREATE USER bob WITH PASSWORD 'bob';
 *     GRANT PROXY.LOGIN ON ROLE 'alice' TO 'bob';
 * </pre>
 * <p>
 * Side effects: none
 */
public class ProxyAuthentication {

    static String[] CONTACT_POINTS = {"127.0.0.1"};
    static int PORT = 9042;

    public static void main(String[] args) {
        // Verify that the query doesn't work without proxy authentication:
        System.out.println("Connecting and querying with bob, acting as bob:");
        connectAndQuery("bob", "bob", null);

        // Run with proxy authentication:
        System.out.println("Connecting and querying with bob, acting as alice:");
        connectAndQuery("bob", "bob", "alice");
    }

    private static void connectAndQuery(String user, String password, String proxy) {
        DseCluster cluster = null;
        try {
            AuthProvider authProvider = (proxy == null)
                    ? new DsePlainTextAuthProvider(user, password)
                    : new DsePlainTextAuthProvider(user, password, proxy);
            cluster = DseCluster.builder()
                    .addContactPoints(CONTACT_POINTS)
                    .withPort(PORT)
                    .withAuthProvider(authProvider)
                    // avoid codec warnings when we create two clusters in a row
                    // (completely unrelated to authentication)
                    .withCodecRegistry(new CodecRegistry())
                    .build();
            DseSession session = cluster.connect();

            // Try to access the table that's only visible to alice:
            session.execute("SELECT * FROM test.foo");

            System.out.println("Query successful");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            if (cluster != null) cluster.close();
        }
    }
}
