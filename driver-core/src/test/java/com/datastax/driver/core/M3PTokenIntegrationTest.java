/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.datastax.driver.core.Token.M3PToken;

public class M3PTokenIntegrationTest extends TokenIntegrationTest {

    public M3PTokenIntegrationTest() {
        super(DataType.bigint(), false);
    }

    @Override
    protected Token.Factory tokenFactory() {
        return M3PToken.FACTORY;
    }
}
