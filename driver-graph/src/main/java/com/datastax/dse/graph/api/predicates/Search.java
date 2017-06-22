/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.graph.api.predicates;

import com.datastax.dse.graph.internal.EditDistance;
import com.datastax.dse.graph.internal.SearchPredicate;
import org.apache.tinkerpop.gremlin.process.traversal.P;

public class Search {

    /**
     * Search any instance of a certain token within the text property targeted (case insensitive).
     *
     * @param value the token to look for.
     * @return a predicate to apply in a {@link org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal}.
     */
    public static P<Object> token(String value) {
        return new P<>(SearchPredicate.token, value);
    }

    /**
     * Search any instance of a certain token prefix within the text property targeted (case insensitive).
     *
     * @param value the token to look for.
     * @return a predicate to apply in a {@link org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal}.
     */
    public static P<Object> tokenPrefix(String value) {
        return new P<>(SearchPredicate.tokenPrefix, value);
    }

    /**
     * Search any instance of the provided regular expression for the targeted property (case insensitive).
     *
     * @param value the token to look for.
     * @return a predicate to apply in a {@link org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal}.
     */
    public static P<Object> tokenRegex(String value) {
        return new P<>(SearchPredicate.tokenRegex, value);
    }

    /**
     * Search for a specific prefix at the beginning of the text property targeted (case sensitive).
     *
     * @param value the value to look for.
     * @return a predicate to apply in a {@link org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal}.
     */
    public static P<Object> prefix(String value) {
        return new P<>(SearchPredicate.prefix, value);
    }

    /**
     * Search for this regular expression inside the text property targeted (case sensitive).
     *
     * @param value the value to look for.
     * @return a predicate to apply in a {@link org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal}.
     */
    public static P<Object> regex(String value) {
        return new P<>(SearchPredicate.regex, value);
    }

    /**
     * Supports finding words which are a within a specific distance away (case insensitive).
     * <p/>
     * Example: the search expression is {@code phrase("Hello world", 2)}
     * <ul>
     * <li>the inserted value "Hello world" is found</li>
     * <li>the inserted value "Hello wild world" is found</li>
     * <li>the inserted value "Hello big wild world" is found</li>
     * <li>the inserted value "Hello the big wild world" is not found</li>
     * <li>the inserted value "Goodbye world" is not found.</li>
     * </ul>
     *
     * @param query    the string to look for in the value
     * @param distance the number of terms allowed between two correct terms to find a value.
     * @return a predicate to apply in a {@link org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal}.
     */
    public static P<Object> phrase(String query, int distance) {
        return new P<>(SearchPredicate.phrase, new EditDistance(query, distance));
    }

    /**
     * Supports fuzzy searches based on the Damerau-Levenshtein Distance, or Edit Distance algorithm
     * (case sensitive).
     * <p/>
     * Example: the search expression is {@code fuzzy("david", 1)}
     * <ul>
     * <li>the inserted value "david" is found</li>
     * <li>the inserted value "dawid" is found</li>
     * <li>the inserted value "davids" is found</li>
     * <li>the inserted value "dewid" is not found</li>
     * </ul>
     *
     * @param query    the string to look for in the value
     * @param distance the number of "uncertainties" allowed for the Leveinshtein algorithm.
     * @return a predicate to apply in a {@link org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal}.
     */
    public static P<Object> fuzzy(String query, int distance) {
        return new P<>(SearchPredicate.fuzzy, new EditDistance(query, distance));
    }

    /**
     * Supports fuzzy searches based on the Damerau-Levenshtein Distance, or Edit Distance algorithm
     * after having tokenized the data stored (case insensitive).
     * <p/>
     * Example: the search expression is {@code tokenFuzzy("david", 1)}
     * <ul>
     * <li>the inserted value "david" is found</li>
     * <li>the inserted value "dawid" is found</li>
     * <li>the inserted value "hello-dawid" is found</li>
     * <li>the inserted value "dewid" is not found</li>
     * </ul>
     *
     * @param query    the string to look for in the value
     * @param distance the number of "uncertainties" allowed for the Leveinshtein algorithm.
     * @return a predicate to apply in a {@link org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal}.
     */
    public static P<Object> tokenFuzzy(String query, int distance) {
        return new P<>(SearchPredicate.tokenFuzzy, new EditDistance(query, distance));
    }
}
