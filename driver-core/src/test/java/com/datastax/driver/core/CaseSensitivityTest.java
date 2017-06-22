/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import org.testng.annotations.Test;

import static com.datastax.driver.core.TestUtils.CREATE_KEYSPACE_SIMPLE_FORMAT;
import static org.testng.Assert.*;

@CCMConfig(clusterProvider = "createClusterBuilderNoDebouncing")
public class CaseSensitivityTest extends CCMTestsSupport {

    @Test(groups = "short")
    public void testCaseInsensitiveKeyspace() throws Throwable {
        String ksName = "MyKeyspace1";
        session().execute(String.format(CREATE_KEYSPACE_SIMPLE_FORMAT, ksName, 1));
        assertExists(ksName, "mykeyspace1");
        assertExists("mykeyspace1", "mykeyspace1");
        assertExists("MYKEYSPACE1", "mykeyspace1");
    }

    @Test(groups = "short")
    public void testCaseSensitiveKeyspace() throws Throwable {
        String ksName = "\"MyKeyspace2\"";
        session().execute(String.format(CREATE_KEYSPACE_SIMPLE_FORMAT, ksName, 1));
        assertExists(ksName, "MyKeyspace2");
        assertExists(Metadata.quote("MyKeyspace2"), "MyKeyspace2");
        assertNotExists("mykeyspace2");
        assertNotExists("MyKeyspace2");
        assertNotExists("MYKEYSPACE2");
    }

    private void assertExists(String fetchName, String realName) {
        KeyspaceMetadata km = cluster().getMetadata().getKeyspace(fetchName);
        assertNotNull(km);
        assertEquals(realName, km.getName());
    }

    private void assertNotExists(String name) {
        assertNull(cluster().getMetadata().getKeyspace(name));
    }


}
