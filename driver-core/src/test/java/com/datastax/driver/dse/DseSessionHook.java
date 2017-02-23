/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse;

import com.datastax.driver.core.Statement;
import com.datastax.driver.dse.graph.GraphOptions;
import com.datastax.driver.dse.graph.GraphStatement;

public class DseSessionHook {
    public static Statement callGenerateCoreStatement(GraphOptions graphOptions, GraphStatement graphStatement) {
        return DefaultDseSession.generateCoreStatement(graphOptions, graphStatement);
    }
}
