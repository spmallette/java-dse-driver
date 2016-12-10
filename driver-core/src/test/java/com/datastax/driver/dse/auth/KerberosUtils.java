/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.auth;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class KerberosUtils {

    /**
     * Executes the given command with KRB5_CONFIG environment variable pointing to the specialized config file
     * for the embedded KDC server.
     */
    public static void executeCommand(String command, EmbeddedADS adsServer) throws IOException {
        Map<String, String> environmentMap = ImmutableMap.<String, String>builder()
                .put("KRB5_CONFIG", adsServer.getKrb5Conf().getAbsolutePath()).build();
        CommandLine cli = CommandLine.parse(command);
        Executor executor = new DefaultExecutor();
        int retValue = executor.execute(cli, environmentMap);
        assertThat(retValue).isZero();
    }

    /**
     * Acquires a ticket into the cache with the tgt using kinit command with the given principal and keytab file.
     */
    public static void acquireTicket(String principal, File keytab, EmbeddedADS adsServer) throws IOException {
        executeCommand(String.format("kinit -t %s -k %s", keytab.getAbsolutePath(), principal), adsServer);
    }

    /**
     * Destroys all tickets in the cache with given principal.
     */
    public static void destroyTicket(EmbeddedADS adsServer) throws IOException {
        executeCommand("kdestroy", adsServer);
    }

    /**
     * Creates a configuration that depends on the ticket cache for authenticating the given user.
     */
    public static Configuration ticketClient(final String principal) {
        return new Configuration() {

            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                Map<String, String> options = ImmutableMap.<String, String>builder()
                        .put("principal", principal)
                        .put("useTicketCache", "true")
                        .put("renewTGT", "true").build();

                return new AppConfigurationEntry[]{
                        new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule",
                                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options)};
            }
        };
    }

    /**
     * Creates a configuration that depends on the given keytab file for authenticating the given user.
     */
    public static Configuration keytabClient(final File keytabFile, final String principal) {
        return new Configuration() {

            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                Map<String, String> options = ImmutableMap.<String, String>builder()
                        .put("principal", principal)
                        .put("useKeyTab", "true")
                        .put("keyTab", keytabFile.getAbsolutePath()).build();

                return new AppConfigurationEntry[]{
                        new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule",
                                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options)};
            }
        };
    }
}
