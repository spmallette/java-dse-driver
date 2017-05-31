/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.auth;

import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.dse.CCMDseTestsSupport;
import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.DseSession;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import java.io.File;

import static com.datastax.driver.core.CreateCCM.TestMode.PER_METHOD;
import static com.datastax.driver.dse.auth.KerberosUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unused")
@CreateCCM(PER_METHOD)
@CCMConfig(createCluster = false, dirtiesContext = true, ccmProvider = "configureCCM")
@DseVersion("5.0.0")
public class DseGSSAPIAuthProviderTest extends CCMDseTestsSupport {

    // Realm for the KDC.
    private static final String realm = "DATASTAX.COM";
    private static final String address = TestUtils.IP_PREFIX + "1";

    private final EmbeddedADS adsServer = EmbeddedADS.builder()
            .withKerberos()
            .withRealm(realm)
            .withAddress(address).build();

    // Principal for DSE service ( = kerberos_options.service_principal)
    private final String servicePrincipal = "dse/" + adsServer.getHostname() + "@" + realm;

    // A non-standard principal for DSE service, to test SASL protocol names
    private final String alternateServicePrincipal = "alternate/" + adsServer.getHostname() + "@" + realm;

    // Principal for the default cassandra user.
    private final String userPrincipal = "cassandra@" + realm;

    // Principal for a user that doesn't exist.
    private final String unknownPrincipal = "unknown@" + realm;

    // Keytabs to use for auth.
    private File userKeytab;
    private File unknownKeytab;
    private File dseKeytab;
    private File alternateKeytab;

    @BeforeClass(groups = "long")
    public void setupKDC() throws Exception {
        if (adsServer.isStarted()) {
            return;
        }
        // Start ldap/kdc server.
        adsServer.start();

        // Create users and keytabs for the DSE principal and cassandra user.
        dseKeytab = adsServer.addUserAndCreateKeytab("dse", "dse", servicePrincipal);
        alternateKeytab = adsServer.addUserAndCreateKeytab("alternate", "alternate", alternateServicePrincipal);
        userKeytab = adsServer.addUserAndCreateKeytab("cassandra", "cassandra", userPrincipal);
        unknownKeytab = adsServer.createKeytab("unknown", "unknown", unknownPrincipal);
    }

    @AfterClass(groups = "long", alwaysRun = true)
    public void teardownKDC() throws Exception {
        adsServer.stop();
    }

    CCMBridge.Builder baseAuthenticationConfiguration() {
        return CCMBridge.builder()
                .withCassandraConfiguration("authenticator", "com.datastax.bdp.cassandra.auth.DseAuthenticator")
                .withDSEConfiguration("kerberos_options.qop", "auth")
                .withDSEConfiguration("authentication_options.enabled", "true")
                .withDSEConfiguration("authentication_options.default_scheme", "kerberos")
                .withJvmArgs("-Dcassandra.superuser_setup_delay_ms=0", "-Djava.security.krb5.conf=" + adsServer.getKrb5Conf().getAbsolutePath());
    }

    public CCMBridge.Builder configureCCM() {
        return baseAuthenticationConfiguration()
                .withDSEConfiguration("kerberos_options.keytab", dseKeytab.getAbsolutePath())
                .withDSEConfiguration("kerberos_options.service_principal", "dse/_HOST@" + realm);
    }

    public CCMBridge.Builder configureAlternateCCM() {
        return baseAuthenticationConfiguration()
                .withDSEConfiguration("kerberos_options.keytab", alternateKeytab.getAbsolutePath())
                .withDSEConfiguration("kerberos_options.service_principal", "alternate/_HOST@" + realm);
    }


    /**
     * Ensures that a Cluster can be established to a DSE server secured with Kerberos and that simple queries can
     * be made using a Subject from a previously established LoginContext.
     *
     * @test_category dse:authentication
     */
    @Test(groups = "long")
    public void should_authenticate_using_subject() throws Exception {
        String protocol = "dse";
        Configuration configuration = keytabClient(userKeytab, userPrincipal);
        LoginContext login = new LoginContext("DseClient", null, null, configuration);
        login.login();
        AuthProvider auth = DseGSSAPIAuthProvider.builder().withSubject(login.getSubject()).build();
        connectAndQuery(auth);
    }

    /**
     * Ensures that a Cluster can be established to a DSE server secured with Kerberos and that simple queries can
     * be made using a client configuration that provides a keytab file.
     *
     * @test_category dse:authentication
     */
    @Test(groups = "long")
    public void should_authenticate_using_kerberos_with_keytab() throws Exception {
        connectAndQuery(keytabClient(userKeytab, userPrincipal));
    }

    /**
     * Ensures that a Cluster can be established to a DSE server secured with Kerberos using
     * an alternate SASL protocol name specified via the 'dse.sasl.protocol' system property.
     *
     * @test_category dse:authentication
     */
    @CCMConfig(ccmProvider = "configureAlternateCCM")
    @Test(groups = "long")
    public void should_authenticate_using_kerberos_with_keytab_and_alternate_service_principal_using_system_property() throws Exception {
        try {
            System.setProperty("dse.sasl.protocol", "alternate");
            connectAndQuery(keytabClient(userKeytab, userPrincipal));
        } finally {
            System.clearProperty("dse.sasl.protocol");
        }
    }

    /**
     * Ensures that a Cluster can be established to a DSE server secured with Kerberos using
     * an alternate SASL protocol name specified {@link DseGSSAPIAuthProvider(String)}.
     *
     * @test_category dse:authentication
     */
    @CCMConfig(ccmProvider = "configureAlternateCCM")
    @Test(groups = "long")
    public void should_authenticate_using_kerberos_with_keytab_and_alternate_service_principal() throws Exception {
        AuthProvider auth = DseGSSAPIAuthProvider.builder()
                .withLoginConfiguration(keytabClient(userKeytab, userPrincipal))
                .withSaslProtocol("alternate")
                .build();
        connectAndQuery(auth);
    }

    /**
     * Ensures that a Cluster can be established to a DSE server secured with Kerberos and that simple queries can
     * be made using a client configuration that uses the ticket cache.  This test will only run on unix platforms
     * since it uses kinit to acquire tickets and kdestroy to destroy them.
     *
     * @test_category dse:authentication
     */
    @Test(groups = "long")
    public void should_authenticate_using_kerberos_with_ticket() throws Exception {
        String osName = System.getProperty("os.name", "").toLowerCase();
        boolean isUnix = osName.contains("mac") || osName.contains("darwin") || osName.contains("nux");
        if (!isUnix) {
            throw new SkipException("This test requires a unix-based platform with kinit & kdestroy installed.");
        }
        acquireTicket(userPrincipal, userKeytab, adsServer);
        try {
            connectAndQuery(ticketClient(userPrincipal));
        } finally {
            destroyTicket(adsServer);
        }
    }

    /**
     * Validates that a {@link NoHostAvailableException} is thrown when using a ticket-based configuration and no
     * such ticket exists in the user's cache.  This is expected because we shouldn't be able to establish connection
     * to a cassandra node if we cannot authenticate.
     *
     * @test_category dse:authentication
     */
    @Test(groups = "long", expectedExceptions = NoHostAvailableException.class)
    public void should_not_authenticate_if_no_ticket_in_cache() throws Exception {
        connectAndQuery(ticketClient(userPrincipal));
    }

    /**
     * Validates that a {@link NoHostAvailableException} is thrown when using a keytab-based configuration and no
     * such user exists for the given principal.  This is expected because we shouldn't be able to establish connection
     * to a cassandra node if we cannot authenticate.
     *
     * @test_category dse:authentication
     */
    @Test(groups = "long", expectedExceptions = NoHostAvailableException.class)
    public void should_not_authenticate_if_keytab_does_not_map_to_valid_principal() throws Exception {
        connectAndQuery(keytabClient(unknownKeytab, unknownPrincipal));
    }

    /**
     * Connects using {@link DseGSSAPIAuthProvider} and the given config file for jaas.
     */
    private void connectAndQuery(Configuration configuration) {
        connectAndQuery(DseGSSAPIAuthProvider.builder().withLoginConfiguration(configuration).build());
    }

    private void connectAndQuery(AuthProvider authProvider) {
        DseCluster cluster = createClusterBuilder()
                .addContactPointsWithPorts(this.getContactPointsWithPorts())
                .withAuthProvider(authProvider).build();

        try {
            DseSession session = cluster.connect();
            Row row = session.execute("select * from system.local").one();
            assertThat(row).isNotNull();
        } finally {
            cluster.close();
        }
    }
}
