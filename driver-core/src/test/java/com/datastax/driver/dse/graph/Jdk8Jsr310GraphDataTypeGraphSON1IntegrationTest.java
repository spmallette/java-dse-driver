/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.dse.IgnoreJDK6Requirement;

@IgnoreJDK6Requirement
@SuppressWarnings("Since15")
@DseVersion("5.0")
public class Jdk8Jsr310GraphDataTypeGraphSON1IntegrationTest extends Jdk8Jsr310GraphDataTypeIntegrationTest {
    // because of a bug in TestNG, Jdk8Jsr310GraphDataTypeIntegrationTest needs to be abstract.  Otherwise if
    // it is skipped on older DSE versions a NPE will be raised when recording its result.
    // See: https://github.com/cbeust/testng/issues/990 for more information.
}
