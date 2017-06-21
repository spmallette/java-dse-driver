/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.RegularStatement;

/**
 * A regular (non-prepared and non batched) graph statement.
 * <p/>
 * This class represents a graph query string along with query options (and optionally values). It can be extended, but
 * {@link SimpleGraphStatement} is provided as a simple implementation to build a {@code RegularGraphStatement} directly
 * from its query string.
 */
public abstract class RegularGraphStatement extends GraphStatement {

    /**
     * Returns the graph query string for this statement.
     *
     * @return the graph query string for this statement.
     */
    public abstract String getQueryString();

    @Override
    public abstract RegularStatement unwrap();

    @Override
    public abstract RegularStatement unwrap(ProtocolVersion protocolVersion);

}

