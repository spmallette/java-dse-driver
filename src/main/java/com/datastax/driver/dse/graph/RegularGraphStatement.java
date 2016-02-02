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
package com.datastax.driver.dse.graph;

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

}

