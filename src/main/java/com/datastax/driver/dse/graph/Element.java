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

import java.util.Map;

/**
 * Base class for vertices and edges.
 */
public abstract class Element {

    protected GraphResult id;

    protected String label;

    protected String type;

    protected Map<String, GraphResult> properties;

    protected Element(GraphResult id, String label, String type, Map<String, GraphResult> properties) {
        this.id = id;
        this.label = label;
        this.type = type;
        this.properties = properties;
    }

    /**
     * Returns the identifier.
     *
     * @return the identifier.
     */
    public GraphResult getId() {
        return this.id;
    }

    /**
     * Returns the label.
     *
     * @return the label.
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Returns the type.
     *
     * @return the type.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Returns the element's properties.
     *
     * @return the properties.
     */
    public Map<String, GraphResult> getProperties() {
        return this.properties;
    }

}
