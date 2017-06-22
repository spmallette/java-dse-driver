/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

@CCMConfig(options = {"-p ByteOrderedPartitioner", "--vnodes"})
public class OPPTokenVnodeIntegrationTest extends TokenIntegrationTest {

    public OPPTokenVnodeIntegrationTest() {
        super(DataType.blob(), true);
    }

    @Override
    protected Token.Factory tokenFactory() {
        return Token.OPPToken.FACTORY;
    }
}
