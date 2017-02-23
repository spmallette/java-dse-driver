/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
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
public interface Path extends Iterable<GraphNode> {

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

    /**
     * Returns the size of this path, that is, the number of steps
     * this path traversed.
     *
     * @return the size of this path.
     */
    int size();

    /**
     * Returns the object associated with the given step of this path.
     * <p>
     * The step index is zero-based, i.e. the first step has index {@code 0}.
     * <p>
     * If the index is out of bounds, this method returns {@code null};
     * no exception will be thrown.
     *
     * @param index the zero-based step index to find the object for.
     * @return the object associated with the given step index.
     */
    GraphNode getObject(int index);

    /**
     * Returns the first object with the given label in this path.
     * <p>
     * If the label is associated with more then one step, then this
     * method returns the object at the first step with that label.
     * <p>
     * If the label does not exist in this path,
     * this method returns {@code null}.
     *
     * @param label the label to find an object for.
     * @return the first object with the given label.
     */
    GraphNode getObject(String label);

    /**
     * Returns the objects with the given label in this path.
     * <p>
     * If the label is unique, the returned list will contain only one element.
     * <p>
     * If the label is associated with more then one step, then the list
     * will contain as many elements as the number of times the label
     * appears in this path's steps, in order.
     * <p>
     * If the label does not exist in this path, an empty list is returned.
     * <p>
     * The returned list is immutable.
     *
     * @param label the label to find objects for.
     * @return the objects with the given label.
     */
    List<GraphNode> getObjects(String label);

    /**
     * Returns whether this path has at least one step
     * with the given label.
     *
     * @param label the label to search.
     * @return {@code true} if this path has at least one step
     * with the given label, {@code false} otherwise.
     */
    boolean hasLabel(String label);
}
