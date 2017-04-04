/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.auth;

import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.AuthenticationException;
import com.datastax.driver.core.exceptions.UnauthorizedException;
import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.dse.CCMDseTestsSupport;
import com.datastax.driver.dse.DseCluster;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

import static com.datastax.driver.core.CreateCCM.TestMode.PER_METHOD;
import static com.datastax.driver.dse.auth.KerberosUtils.keytabClient;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Tests for proxy authentication (JAVA-1264)
 */
@CreateCCM(PER_METHOD)
@CCMConfig(ccmProvider = "configureCCM")
@DseVersion("5.1.0")
public class DseProxyAuthenticationTest extends CCMDseTestsSupport {

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

    @BeforeClass(groups = "long")
    public void setupKDC() throws Exception {
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

    @SuppressWarnings("unused")
    public CCMBridge.Builder configureCCM() {
        // Specify authentication options together as a literal yaml string.  This is needed to express
        // other_schemes as a list.
        String authenticationOptions = "" +
                "authentication_options:\n" +
                "  enabled: true\n" +
                "  default_scheme: kerberos\n" +
                "  other_schemes:\n" +
                "    - internal";

        return CCMBridge.builder()
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
        return super.createClusterBuilder()
                .withAuthProvider(new DsePlainTextAuthProvider("cassandra", "cassandra"));
    }

    @Override
    public void onTestContextInitialized() {
        // executed as user cassandra
        execute(
                "CREATE ROLE IF NOT EXISTS alice WITH PASSWORD = 'alice' AND LOGIN = FALSE",
                "CREATE ROLE IF NOT EXISTS ben WITH PASSWORD = 'ben' AND LOGIN = TRUE",
                "CREATE ROLE IF NOT EXISTS 'bob@DATASTAX.COM' WITH LOGIN = TRUE",
                "CREATE ROLE IF NOT EXISTS 'charlie@DATASTAX.COM' WITH PASSWORD = 'charlie' AND LOGIN = TRUE",
                "CREATE ROLE IF NOT EXISTS steve WITH PASSWORD = 'steve' AND LOGIN = TRUE",
                "CREATE KEYSPACE IF NOT EXISTS aliceks WITH REPLICATION = {'class':'SimpleStrategy', 'replication_factor':'1'}",
                "CREATE TABLE IF NOT EXISTS aliceks.alicetable (key text PRIMARY KEY, value text)",
                "INSERT INTO aliceks.alicetable (key, value) VALUES ('hello', 'world')",
                "GRANT ALL ON KEYSPACE aliceks TO alice",
                "GRANT EXECUTE ON ALL AUTHENTICATION SCHEMES TO 'ben'",
                "GRANT EXECUTE ON ALL AUTHENTICATION SCHEMES TO 'bob@DATASTAX.COM'",
                "GRANT EXECUTE ON ALL AUTHENTICATION SCHEMES TO 'steve'",
                "GRANT EXECUTE ON ALL AUTHENTICATION SCHEMES TO 'charlie@DATASTAX.COM'",
                "GRANT PROXY.LOGIN ON ROLE 'alice' TO 'ben'",
                "GRANT PROXY.LOGIN ON ROLE 'alice' TO 'bob@DATASTAX.COM'",
                "GRANT PROXY.EXECUTE ON ROLE 'alice' TO 'steve'",
                "GRANT PROXY.EXECUTE ON ROLE 'alice' TO 'charlie@DATASTAX.COM'"
                // ben and bob are allowed to login as alice, but not execute as alice.
                // charlie and steve are allowed to execute as alice, but not login as alice.
        );
    }

    /**
     * Validates that a connection may be successfully made as user 'alice' using the credentials of a user 'ben'
     * using {@link DsePlainTextAuthProvider} assuming ben has PROXY.LOGIN authorization on alice.
     *
     * @jira_ticket JAVA-1264
     * @test_category dse:authentication
     */
    @Test(groups = "long")
    public void should_allow_plain_text_authorized_user_to_login_as() throws Exception {
        AuthProvider authProvider = new DsePlainTextAuthProvider("ben", "ben", "alice");
        Row row = connectAndQuery(authProvider, null);
        assertThat(row).isNotNull();
    }

    /**
     * Validates that a connection may successfully made as user 'alice' using the credentials of a principal
     * 'bob@DATASTAX.COM' using {@link DseGSSAPIAuthProvider} assuming 'bob@DATASTAX.COM' has PROXY.LOGIN authorization
     * on alice.
     *
     * @jira_ticket JAVA-1264
     * @test_category dse:authentication
     */
    @Test(groups = "long")
    public void should_allow_kerberos_authorized_user_to_login_as() throws Exception {
        AuthProvider authProvider = DseGSSAPIAuthProvider.builder()
                .withLoginConfiguration(keytabClient(bobKeytab, bobPrincipal))
                .withAuthorizationId("alice")
                .build();
        Row row = connectAndQuery(authProvider, null);
        assertThat(row).isNotNull();
    }

    /**
     * Validates that a connection does not succeed as user 'alice' using the credentials of a user 'steve'
     * assuming 'steve' does not have PROXY.LOGIN authorization on alice.
     *
     * @jira_ticket JAVA-1264
     * @test_category dse:authentication
     */
    @Test(groups = "long", expectedExceptions = AuthenticationException.class)
    public void should_not_allow_plain_text_unauthorized_user_to_login_as() throws Exception {
        AuthProvider authProvider = new DsePlainTextAuthProvider("steve", "steve", "alice");
        connectAndQuery(authProvider, null);
    }

    /**
     * Validates that a connection does not succeed as user 'alice' using the credentials of a principal
     * 'charlie@DATASTAX.COM' assuming 'charlie@DATASTAX.COM' does not have PROXY.LOGIN authorization on alice.
     *
     * @jira_ticket JAVA-1264
     * @test_category dse:authentication
     */
    @Test(groups = "long", expectedExceptions = AuthenticationException.class)
    public void should_not_allow_kerberos_unauthorized_user_to_login_as() throws Exception {
        AuthProvider authProvider = DseGSSAPIAuthProvider.builder()
                .withLoginConfiguration(keytabClient(charlieKeytab, charliePrincipal))
                .withAuthorizationId("alice")
                .build();
        connectAndQuery(authProvider, null);
    }

    /**
     * Validates that a query may be successfully made as user 'alice' using a {@link DseCluster} that is authenticated
     * to user 'steve' using {@link DsePlainTextAuthProvider} assuming steve has PROXY.EXECUTE authorization on alice.
     *
     * @jira_ticket JAVA-1264
     * @test_category dse:authentication
     */
    @Test(groups = "long")
    public void should_allow_plain_text_authorized_user_to_execute_as() throws Exception {
        DsePlainTextAuthProvider authProvider = new DsePlainTextAuthProvider("steve", "steve");
        Row row = connectAndQuery(authProvider, "alice");
        assertThat(row).isNotNull();
    }

    /**
     * Validates that a batch may be successfully made as user 'alice' using a {@link DseCluster} that is authenticated
     * to user 'steve' using {@link DsePlainTextAuthProvider} assuming steve has PROXY.EXECUTE authorization on alice.
     *
     * @jira_ticket JAVA-1391
     * @test_category dse:authentication
     */
    @Test(groups = "long")
    public void should_allow_plain_text_authorized_user_to_execute_batch_as() throws Exception {
        DsePlainTextAuthProvider authProvider = new DsePlainTextAuthProvider("steve", "steve");
        connectAndBatchQuery(authProvider, "alice");
    }

    /**
     * Validates that a query may be successfully made as user 'alice' using a {@link DseCluster} that is authenticated
     * to principal 'charlie@DATASTAX.COM' using {@link DseGSSAPIAuthProvider} assuming charlie@DATASTAX.COM has
     * PROXY.EXECUTE authorization on alice.
     *
     * @jira_ticket JAVA-1264
     * @test_category dse:authentication
     */
    @Test(groups = "long")
    public void should_allow_kerberos_authorized_user_to_execute_as() throws Exception {
        DseGSSAPIAuthProvider authProvider = new DseGSSAPIAuthProvider(keytabClient(charlieKeytab, charliePrincipal));
        Row row = connectAndQuery(authProvider, "alice");
        assertThat(row).isNotNull();
    }

    /**
     * Validates that a query may not be made as user 'alice' using a {@link DseCluster} that is authenticated to user
     * 'ben' if ben does not have PROXY.EXECUTE authorization on alice.
     *
     * @jira_ticket JAVA-1264
     * @test_category dse:authentication
     */
    @Test(groups = "long")
    public void should_not_allow_plain_text_unauthorized_user_to_execute_as() throws Exception {
        DsePlainTextAuthProvider authProvider = new DsePlainTextAuthProvider("ben", "ben");
        try {
            connectAndQuery(authProvider, "alice");
            fail("Should have thrown an exception");
        } catch (UnauthorizedException e) {
            verifyException(e, "ben");
        }
    }

    /**
     * Validates that a batch query may not be made as user 'alice' using a {@link DseCluster} that is authenticated to
     * user 'ben' if ben does not have PROXY.EXECUTE authorization on alice.
     *
     * @jira_ticket JAVA-1264
     * @test_category dse:authentication
     */
    @Test(groups = "long")
    public void should_not_allow_plain_text_unauthorized_user_to_execute_batch_as() throws Exception {
        DsePlainTextAuthProvider authProvider = new DsePlainTextAuthProvider("ben", "ben");
        try {
            connectAndBatchQuery(authProvider, "alice");
            fail("Should have thrown an exception");
        } catch (UnauthorizedException e) {
            verifyException(e, "ben");
        }
    }

    /**
     * Validates that a query may not be made as user 'alice' using a {@link DseCluster} that is authenticated
     * to principal 'bob@DATASTAX.COM' using {@link DseGSSAPIAuthProvider} if bob@DATASTAX.COM does not have
     * PROXY.EXECUTE authorization on alice.
     *
     * @jira_ticket JAVA-1264
     * @test_category dse:authentication
     */
    @Test(groups = "long")
    public void should_not_allow_kerberos_unauthorized_user_to_execute_as() throws Exception {
        DseGSSAPIAuthProvider authProvider = new DseGSSAPIAuthProvider(keytabClient(bobKeytab, bobPrincipal));
        try {
            connectAndQuery(authProvider, "alice");
            fail("Should have thrown an exception");
        } catch (UnauthorizedException e) {
            verifyException(e, bobPrincipal);
        }
    }

    private Row connectAndQuery(AuthProvider authProvider, String as) {
        DseCluster cluster = createClusterBuilder()
                .addContactPointsWithPorts(this.getContactPointsWithPorts())
                .withAuthProvider(authProvider)
                .build();
        try {
            Statement statement = new SimpleStatement("select * from aliceks.alicetable");
            if (as != null) {
                statement = statement.executingAs(as);
            }
            return cluster.connect().execute(statement).one();
        } finally {
            cluster.close();
        }
    }

    private void connectAndBatchQuery(AuthProvider authProvider, String as) {
        DseCluster cluster = createClusterBuilder()
                .addContactPointsWithPorts(this.getContactPointsWithPorts())
                .withAuthProvider(authProvider)
                .build();
        try {
            BatchStatement statement = new BatchStatement();
            statement.add(new SimpleStatement("delete from aliceks.alicetable where key = 'doesnotexist'"));
            if (as != null) {
                statement.executingAs(as);
            }
            cluster.connect().execute(statement).one();
        } finally {
            cluster.close();
        }
    }

    private void verifyException(UnauthorizedException e, String user) {
        assertThat(e.getMessage()).isEqualTo(
                String.format("Either '%s' does not have permission to execute queries as 'alice' " +
                        "or that role does not exist. " +
                        "Run 'GRANT PROXY.EXECUTE ON ROLE 'alice' TO '%s' as an administrator " +
                        "if you wish to allow this.", user, user));
    }

}
