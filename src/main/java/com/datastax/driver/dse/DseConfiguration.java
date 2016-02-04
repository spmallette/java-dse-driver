/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse;

import com.datastax.driver.core.Configuration;
import com.datastax.driver.dse.graph.GraphOptions;

/**
 * The configuration of a {@link DseCluster}.
 * <p/>
 * This class extends the CQL driver's {@link Configuration} to add DSE-specific options.
 */
public class DseConfiguration extends Configuration {

    private final GraphOptions graphOptions;

    DseConfiguration(Configuration toCopy, GraphOptions graphOptions) {
        super(toCopy);
        this.graphOptions = graphOptions;
    }

    /**
     * Returns the default graph options to use for the cluster.
     *
     * @return the default graph options.
     */
    public GraphOptions getGraphOptions() {
        return graphOptions;
    }

}
