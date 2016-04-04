/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.google.common.collect.Iterators;
import org.apache.tinkerpop.gremlin.process.traversal.Path;

import java.util.*;

class TinkerPath implements Path {

    List<Set<String>> labels;

    List<Object> objects;

    TinkerPath() {
    }

    @Override
    public TinkerPath extend(final Object object, final Set<String> labels) {
        throw new UnsupportedOperationException("Cannot extend this path object");
    }

    @Override
    public TinkerPath extend(final Set<String> labels) {
        throw new UnsupportedOperationException("Cannot extend this path object");
    }

    @Override
    public List<Object> objects() {
        return Collections.unmodifiableList(objects);
    }

    @Override
    public List<Set<String>> labels() {
        return Collections.unmodifiableList(labels);
    }

    @Override
    public Iterator<Object> iterator() {
        return Iterators.unmodifiableIterator(objects().iterator());
    }

    @Override
    @SuppressWarnings("CloneDoesntCallSuperClone,CloneDoesntDeclareCloneNotSupportedException")
    public TinkerPath clone() {
        TinkerPath clone = new TinkerPath();
        clone.labels = new ArrayList<Set<String>>(labels);
        clone.objects = new ArrayList<Object>(objects);
        return clone;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Path))
            return false;
        Path otherPath = (Path) other;
        if (otherPath.size() != this.size())
            return false;
        for (int i = this.size() - 1; i >= 0; i--) {
            if (!this.objects.get(i).equals(otherPath.get(i)))
                return false;
            if (!this.labels.get(i).equals(otherPath.labels().get(i)))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return this.objects.hashCode();
    }

}
