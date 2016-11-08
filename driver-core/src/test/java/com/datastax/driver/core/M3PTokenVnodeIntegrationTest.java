/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

@CCMConfig(options = "--vnodes")
public class M3PTokenVnodeIntegrationTest extends TokenIntegrationTest {

    public M3PTokenVnodeIntegrationTest() {
        super(DataType.bigint(), true);
    }

    @Override
    protected Token.Factory tokenFactory() {
        return Token.M3PToken.FACTORY;
    }
}
