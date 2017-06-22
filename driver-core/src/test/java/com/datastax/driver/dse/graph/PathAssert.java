/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractListAssert;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PathAssert extends AbstractAssert<PathAssert, Path> {

    public PathAssert(Path actual) {
        super(actual, PathAssert.class);
    }

    public GraphNodeAssert object(int i) {
        assertThat(actual.getObjects().size()).isGreaterThanOrEqualTo(i);
        return new GraphNodeAssert(actual.getObject(i));
    }

    public PathAssert hasLabel(String label) {
        assertThat(actual.hasLabel(label)).isTrue();
        return myself;
    }

    public PathAssert doesNotHaveLabel(String label) {
        assertThat(actual.hasLabel(label)).isFalse();
        return myself;
    }

    public PathAssert hasLabel(int i, String... labels) {
        assertThat(actual.getLabels().size()).isGreaterThanOrEqualTo(i);
        assertThat(actual.getLabels().get(i)).containsOnly(labels);
        return myself;
    }

    public PathAssert hasNoLabel(int i) {
        return hasLabel(i);
    }

    public GraphNodeAssert object(String label) {
        return new GraphNodeAssert(actual.getObject(label));
    }

    public AbstractListAssert<?, ? extends List<GraphNode>, GraphNode> objects(String label) {
        return assertThat(actual.getObjects(label));
    }
}
