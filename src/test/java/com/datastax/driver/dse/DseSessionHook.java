/*
 *      Copyright (C) 2012-2016 DataStax Inc.
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
