/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import java.util.List;
import java.util.Set;

/**
 * A walk through a graph as defined by a traversal.
 * <p/>
 * Clients typically obtain instances of this class by calling {@link GraphNode#asPath()}, for example:
 * <pre>
 *     GraphNode n = dseSession.executeGraph("g.V().hasLabel('some_vertex').outE().inV().path()").one();
 *     Path path = n.asPath();
 * </pre>
 */
public interface Path {

    /**
     * Returns the sets of labels of the steps traversed
     * by this path, or an empty list, if this path is empty.
     * <p>
     * The returned list is immutable.
     *
     * @return the sets of labels of the steps traversed
     * by this path.
     */
    List<Set<String>> getLabels();

    /**
     * Returns the objects traversed by this path,
     * or an empty list, if this path is empty.
     * <p>
     * The returned list is immutable.
     *
     * @return the objects traversed by this path.
     */
    List<GraphNode> getObjects();
}
