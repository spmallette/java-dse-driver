/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.datastax.driver.core.exceptions.UnsupportedFeatureException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Test ResultSet paging correct behavior.
 */
public class FetchingTest extends CCMTestsSupport {

    @Override
    public void onTestContextInitialized() {
        execute("CREATE TABLE test (k text, v int, PRIMARY KEY (k, v))");
    }

    @Test(groups = "short")
    public void simplePagingTest() {
        try {
            // Insert data
            String key = "paging_test";
            for (int i = 0; i < 100; i++)
                session().execute(String.format("INSERT INTO test (k, v) VALUES ('%s', %d)", key, i));

            SimpleStatement st = new SimpleStatement(String.format("SELECT v FROM test WHERE k='%s'", key));
            st.setFetchSize(5); // Ridiculously small fetch size for testing purpose. Don't do at home.
            ResultSet rs = session().execute(st);

            assertFalse(rs.isFullyFetched());

            for (int i = 0; i < 100; i++) {
                // isExhausted makes sure we do fetch if needed
                assertFalse(rs.isExhausted());
                assertEquals(rs.getAvailableWithoutFetching(), 5 - (i % 5));
                assertEquals(rs.one().getInt(0), i);
            }

            assertTrue(rs.isExhausted());
            assertTrue(rs.isFullyFetched());

        } catch (UnsupportedFeatureException e) {
            // This is expected when testing the protocol v1
            assertEquals(cluster().getConfiguration().getProtocolOptions().getProtocolVersion(), ProtocolVersion.V1);
        }
    }
}
