/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.exceptions;

import org.testng.annotations.Test;

import java.net.InetSocketAddress;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class ConnectionExceptionTest {

    /**
     * @jira_ticket JAVA-1139
     */
    @Test(groups = "unit")
    public void getHost_should_return_null_if_address_is_null() {
        assertNull(new ConnectionException(null, "Test message").getHost());
    }

    /**
     * @jira_ticket JAVA-1139
     */
    @Test(groups = "unit")
    public void getMessage_should_return_message_if_address_is_null() {
        assertEquals(new ConnectionException(null, "Test message").getMessage(), "Test message");
    }

    /**
     * @jira_ticket JAVA-1139
     */
    @Test(groups = "unit")
    public void getMessage_should_return_message_if_address_is_unresolved() {
        assertEquals(new ConnectionException(InetSocketAddress.createUnresolved("127.0.0.1", 9042), "Test message").getMessage(), "[127.0.0.1:9042] Test message");
    }

    /**
     * @jira_ticket JAVA-1139
     */
    @Test(groups = "unit")
    public void getMessage_should_return_message_if_address_is_resolved() {
        assertEquals(new ConnectionException(new InetSocketAddress("127.0.0.1", 9042), "Test message").getMessage(), "[/127.0.0.1:9042] Test message");
    }
}
