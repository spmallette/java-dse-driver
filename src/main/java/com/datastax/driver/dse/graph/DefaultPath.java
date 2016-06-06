/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Set;

class DefaultPath implements Path {

    private static final Function<Set<String>, Set<String>> TO_UNMODIFIABLE_SET = new Function<Set<String>, Set<String>>() {
        @Override
        public Set<String> apply(Set<String> input) {
            return Collections.unmodifiableSet(input);
        }
    };

    List<Set<String>> labels;

    List<GraphNode> objects;

    DefaultPath() {
    }

    @Override
    public List<Set<String>> getLabels() {
        return labels == null || labels.isEmpty()
                ? Collections.<Set<String>>emptyList()
                : Collections.unmodifiableList(Lists.transform(labels, TO_UNMODIFIABLE_SET));
    }

    @Override
    public List<GraphNode> getObjects() {
        return objects == null || objects.isEmpty()
                ? Collections.<GraphNode>emptyList()
                : Collections.unmodifiableList(objects);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Path)) return false;
        Path that = (Path) o;
        return Objects.equal(getLabels(), that.getLabels()) &&
                Objects.equal(getObjects(), that.getObjects());
    }

    @Override
    public int hashCode() {
        // getLabels() deliberately left out for faster hashcodes
        return Objects.hashCode(getObjects());
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("labels", getLabels())
                .add("objects", getObjects())
                .toString();
    }
}
