/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.datastax.driver.core.Token.RPToken;

@CCMConfig(options = "-p RandomPartitioner")
public class RPTokenIntegrationTest extends TokenIntegrationTest {

    public RPTokenIntegrationTest() {
        super(DataType.varint(), false);
    }

    @Override
    protected Token.Factory tokenFactory() {
        return RPToken.FACTORY;
    }
}
