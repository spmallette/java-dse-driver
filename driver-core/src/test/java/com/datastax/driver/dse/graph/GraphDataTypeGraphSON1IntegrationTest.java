/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.utils.DseVersion;

@DseVersion("5.0")
public class GraphDataTypeGraphSON1IntegrationTest extends GraphDataTypeIntegrationTest {
    // because of a bug in TestNG, GraphDataTypeIntegrationTest needs to be abstract.  Otherwise if
    // it is skipped on older DSE versions a NPE will be raised when recording its result.
    // See: https://github.com/cbeust/testng/issues/990 for more information.
}
