/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.CCMBridge;
import com.datastax.driver.core.TestUtils;
import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.auth.DseGSSAPIAuthProvider;
import com.datastax.driver.dse.auth.DsePlainTextAuthProvider;
import com.datastax.driver.dse.auth.EmbeddedADS;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.Method;

import static com.datastax.driver.dse.auth.KerberosUtils.keytabClient;
import static org.assertj.core.api.Assertions.assertThat;

@DseVersion("5.1.0")
public class GraphProxyAuthenticationTest extends CCMGraphTestsSupport {

    // Realm for the KDC.
    private static final String realm = "DATASTAX.COM";
    private static final String address = TestUtils.IP_PREFIX + "1";

    private final EmbeddedADS adsServer = EmbeddedADS.builder()
            .withKerberos()
            .withRealm(realm)
            .withAddress(address)
            .build();

    // Principal for DSE service ( = kerberos_options.service_principal)
    private final String dsePrincipal = "dse/" + adsServer.getHostname() + "@" + realm;

    private final String bobPrincipal = "bob@" + realm;
    private final String charliePrincipal = "charlie@" + realm;

    // Keytabs to use for auth.
    private File dseKeytab;
    private File bobKeytab;
    private File charlieKeytab;

    @Override
    protected void initTestContext(Object testInstance, Method testMethod) throws Exception {
        setupKDC();
        super.initTestContext(testInstance, testMethod);
    }

    void setupKDC() throws Exception {
        if (adsServer.isStarted()) {
            return;
        }
        // Start ldap/kdc server.
        adsServer.start();
        // Create users and keytabs for the DSE principal and cassandra user.
        dseKeytab = adsServer.addUserAndCreateKeytab("dse", "dse", dsePrincipal);
        bobKeytab = adsServer.addUserAndCreateKeytab("bob", "bob", bobPrincipal);
        charlieKeytab = adsServer.addUserAndCreateKeytab("charlie", "charlie", charliePrincipal);
    }

    @AfterClass(groups = "long", alwaysRun = true)
    public void teardownKDC() throws Exception {
        adsServer.stop();
    }

    @Override
    public void onTestContextInitialized() {
        super.onTestContextInitialized();
        // executed as user cassandra
        executeGraph(GraphFixtures.modern);
        execute(
                "CREATE ROLE IF NOT EXISTS guser WITH PASSWORD = 'guser' AND LOGIN = FALSE",
                "CREATE ROLE IF NOT EXISTS ben WITH PASSWORD = 'ben' AND LOGIN = TRUE",
                "CREATE ROLE IF NOT EXISTS 'bob@DATASTAX.COM' WITH LOGIN = TRUE",
                "CREATE ROLE IF NOT EXISTS 'charlie@DATASTAX.COM' WITH PASSWORD = 'charlie' AND LOGIN = TRUE",
                "CREATE ROLE IF NOT EXISTS steve WITH PASSWORD = 'steve' AND LOGIN = TRUE",
                String.format("GRANT ALL ON KEYSPACE %s TO guser", graphName()),
                String.format("GRANT ALL ON KEYSPACE %s_pvt TO guser", graphName()),
                String.format("GRANT ALL ON KEYSPACE %s_system TO guser", graphName()),
                "GRANT EXECUTE ON ALL AUTHENTICATION SCHEMES TO 'ben'",
                "GRANT EXECUTE ON ALL AUTHENTICATION SCHEMES TO 'bob@DATASTAX.COM'",
                "GRANT EXECUTE ON ALL AUTHENTICATION SCHEMES TO 'steve'",
                "GRANT EXECUTE ON ALL AUTHENTICATION SCHEMES TO 'charlie@DATASTAX.COM'",
                "GRANT PROXY.LOGIN ON ROLE 'guser' TO 'ben'",
                "GRANT PROXY.LOGIN ON ROLE 'guser' TO 'bob@DATASTAX.COM'",
                "GRANT PROXY.EXECUTE ON ROLE 'guser' TO 'steve'",
                "GRANT PROXY.EXECUTE ON ROLE 'guser' TO 'charlie@DATASTAX.COM'"
                // ben and bob are allowed to login as alice, but not execute as guser.
                // charlie and steve are allowed to execute as alice, but not login as guser.
        );
    }

    @Override
    public CCMBridge.Builder configureCCM() {
        // Specify authentication options together as a literal yaml string.  This is needed to express
        // other_schemes as a list.
        String authenticationOptions = "" +
                "authentication_options:\n" +
                "  enabled: true\n" +
                "  default_scheme: kerberos\n" +
                "  other_schemes:\n" +
                "    - internal";
        return super.configureCCM()
                .withCassandraConfiguration("authorizer", "com.datastax.bdp.cassandra.auth.DseAuthorizer")
                .withCassandraConfiguration("authenticator", "com.datastax.bdp.cassandra.auth.DseAuthenticator")
                .withDSEConfiguration(authenticationOptions)
                .withDSEConfiguration("authorization_options.enabled", true)
                .withDSEConfiguration("kerberos_options.keytab", dseKeytab.getAbsolutePath())
                .withDSEConfiguration("kerberos_options.service_principal", "dse/_HOST@" + realm)
                .withDSEConfiguration("kerberos_options.qop", "auth")
                .withJvmArgs(
                        "-Dcassandra.superuser_setup_delay_ms=0",
                        "-Djava.security.krb5.conf=" + adsServer.getKrb5Conf().getAbsolutePath());
    }

    @Override
    public DseCluster.Builder createClusterBuilder() {
        return super.createClusterBuilder().withAuthProvider(new DsePlainTextAuthProvider("cassandra", "cassandra"));
    }

    /**
     * Ensure that graph queries can be made using proxy authentication with {@link DsePlainTextAuthProvider}.
     *
     * @test_category dse:graph
     */
    @Test(groups = "long")
    public void should_make_traversal_using_plain_text_with_proxy_authentication() {
        query(new DsePlainTextAuthProvider("ben", "ben", "guser"));
    }

    /**
     * Ensure that graph queries can be made using proxy execution with {@link DsePlainTextAuthProvider}.
     *
     * @test_category dse:graph
     */
    @Test(groups = "long")
    public void should_make_traversal_using_plain_text_with_proxy_execution() {
        queryWithExecuteAs(new DsePlainTextAuthProvider("steve", "steve"));
    }

    /**
     * Ensure that graph queries can be made using proxy authentication with {@link DseGSSAPIAuthProvider}.
     *
     * @test_category dse:graph
     */
    @Test(groups = "long")
    public void should_make_traversal_using_kerberos_with_proxy_authentication() {
        query(DseGSSAPIAuthProvider.builder()
                .withLoginConfiguration(keytabClient(bobKeytab, bobPrincipal))
                .withAuthorizationId("guser")
                .build());
    }

    /**
     * Ensure that graph queries can be made using proxy execution with {@link DseGSSAPIAuthProvider}.
     *
     * @test_category dse:graph
     */
    @Test(groups = "long")
    public void should_make_traversal_using_kerberos_with_proxy_execution() {
        queryWithExecuteAs(DseGSSAPIAuthProvider.builder()
                .withLoginConfiguration(keytabClient(charlieKeytab, charliePrincipal))
                .build());
    }

    private void query(AuthProvider authProvider) {
        query(authProvider, false);
    }

    private void queryWithExecuteAs(AuthProvider authProvider) {
        query(authProvider, true);
    }

    private void query(AuthProvider authProvider, boolean executeAs) {
        DseCluster cluster = super.createClusterBuilder()
                .addContactPointsWithPorts(this.getContactPointsWithPorts())
                .withGraphOptions(new GraphOptions().setGraphName(graphName()))
                .withAuthProvider(authProvider)
                .build();

        try {
            DseSession session = cluster.connect();
            GraphStatement statement = new SimpleGraphStatement("g.V().count()");
            if (executeAs) {
                statement = statement.executingAs("guser");
            }
            int count = session.executeGraph(statement).one().asInt();
            assertThat(count).isEqualTo(6);
        } finally {
            cluster.close();
        }
    }
}
