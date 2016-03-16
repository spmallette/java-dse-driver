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

}
