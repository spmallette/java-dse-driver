/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

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
