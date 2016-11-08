/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

@CCMConfig(options = {"-p RandomPartitioner", "--vnodes"})
public class RPTokenVnodeIntegrationTest extends TokenIntegrationTest {

    public RPTokenVnodeIntegrationTest() {
        super(DataType.varint(), true);
    }

    @Override
    protected Token.Factory tokenFactory() {
        return Token.RPToken.FACTORY;
    }
}
