/*
 *      Copyright (C) 2012-2015 DataStax Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.datastax.driver;

import com.datastax.driver.core.Configuration;
import com.datastax.driver.graph.GraphOptions;

/**
 * The configuration of a {@link DseCluster}.
 *
 * @see Configuration
 */
public class DseConfiguration extends Configuration {

    private final GraphOptions graphOptions;

    DseConfiguration(Configuration toCopy, GraphOptions graphOptions) {
        super(toCopy);
        this.graphOptions = graphOptions;
    }

    /**
     * Returns the {@link GraphOptions} instance for this configuration.
     *
     * @return the {@link GraphOptions} instance for this configuration.
     */
    public GraphOptions getGraphOptions() {
        return graphOptions;
    }

}
