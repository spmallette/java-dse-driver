/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.Statement;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleGraphStatementTest {

    private static final String PROXY_EXECUTE = "ProxyExecute";

    @Test(groups = "unit")
    public void should_retain_executeAs_payload_when_unwrapped() throws Exception {
        GraphStatement graphStatement = new SimpleGraphStatement("g.V()").executingAs("tom");

        Statement statement = graphStatement.unwrap();
        Map<String, ByteBuffer> payload = statement.getOutgoingPayload();

        assertThat(payload.containsKey(PROXY_EXECUTE));
        assertThat(new String(payload.get(PROXY_EXECUTE).array(), "UTF-8")).isEqualTo("tom");
    }
}
