/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.graph.api.predicates;

import com.datastax.dse.graph.internal.SearchPredicate;
import org.apache.tinkerpop.gremlin.process.traversal.P;

public class Search {

    /**
     * Search any instance of a certain token within the text property targeted. It is not case sensitive.
     *
     * @param value the token to look for.
     * @param <V> the type of the entity to use. (for Search, it is most likely a
     *           string, or an object with a properly defined toString() method)
     * @return a predicate to apply in a Traversal.
     */
    public static <V> P<V> token(V value) {
        return new P(SearchPredicate.token, value);
    }

    /**
     * Search any instance of a certain token prefix withing the text property targeted. It is not case sensitive.
     *
     * @param value the token to look for.
     * @param <V> the type of the entity to use. (for Search, it is most likely a
     *           string, or an object with a properly defined toString() method)
     * @return a predicate to apply in a Traversal.
     */
    public static <V> P<V> tokenPrefix(V value) {
        return new P(SearchPredicate.tokenPrefix, value);
    }

    /**
     * Search any instance of the provided regular expression for the targeted property.
     *
     * @param value the token to look for.
     * @param <V> the type of the entity to use. (for Search, it is most likely a
     *           string, or an object with a properly defined toString() method)
     * @return a predicate to apply in a Traversal.
     */
    public static <V> P<V> tokenRegex(V value) {
        return new P(SearchPredicate.tokenRegex, value);
    }

    /**
     * Search for a specific prefix at the beginning of the text property targeted.
     *
     * @param value the value to look for.
     * @param <V> the type of the entity to use. (for Search, it is most likely a
     *           string, or an object with a properly defined toString() method)
     * @return a predicate to apply in a Traversal.
     */
    public static <V> P<V> prefix(V value) {
        return new P(SearchPredicate.prefix, value);
    }

    /**
     * Search for this regular expression inside the text property targeted.
     *
     * @param value the value to look for.
     * @param <V> the type of the entity to use. (for Search, it is most likely a
     *           string, or an object with a properly defined toString() method)
     * @return a predicate to apply in a Traversal.
     */
    public static <V> P<V> regex(V value) {
        return new P(SearchPredicate.regex, value);
    }
}
