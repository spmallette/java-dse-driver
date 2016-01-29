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
package com.datastax.driver.graph;

import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class PathAssert extends AbstractAssert<PathAssert, Path> {
    protected PathAssert(Path actual) {
        super(actual, PathAssert.class);
    }

    public GraphResultAssert object(int i) {
        assertThat(actual.getObjects().size()).isGreaterThanOrEqualTo(i);
        return new GraphResultAssert(actual.getObjects().get(i));
    }

    public PathAssert hasLabel(int i, String... labels) {
        assertThat(actual.getLabels().size()).isGreaterThanOrEqualTo(i);
        assertThat(actual.getLabels().get(i)).containsExactly(labels);
        return myself;
    }
}
