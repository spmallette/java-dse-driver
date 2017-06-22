/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.examples.basic;

import com.datastax.driver.core.*;

/**
 * Gathers information about a Cassandra cluster's topology (which nodes belong to the cluster) and schema (what
 * keyspaces, tables, etc. exist in this cluster).
 * <p/>
 * Preconditions:
 * - a Cassandra cluster is running and accessible through the contacts points identified by CONTACT_POINTS and PORT.
 * <p/>
 * Side effects: none.
 *
 * @see <a href="http://datastax.github.io/java-driver/manual/">Java driver online manual</a>
 */
public class ReadTopologyAndSchemaMetadata {

    static String[] CONTACT_POINTS = {"127.0.0.1"};
    static int PORT = 9042;

    public static void main(String[] args) {

        Cluster cluster = null;
        try {
            cluster = Cluster.builder()
                    .addContactPoints(CONTACT_POINTS).withPort(PORT)
                    .build();

            Metadata metadata = cluster.getMetadata();
            System.out.printf("Connected to cluster: %s%n", metadata.getClusterName());

            for (Host host : metadata.getAllHosts()) {
                System.out.printf("Datatacenter: %s; Host: %s; Rack: %s%n",
                        host.getDatacenter(), host.getAddress(), host.getRack());
            }

            for (KeyspaceMetadata keyspace : metadata.getKeyspaces()) {
                for (TableMetadata table : keyspace.getTables()) {
                    System.out.printf("Keyspace: %s; Table: %s%n",
                            keyspace.getName(), table.getName());
                }
            }

        } finally {
            if (cluster != null)
                cluster.close();
        }
    }
}
