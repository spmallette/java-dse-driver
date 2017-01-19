/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.graph.internal;

import com.datastax.driver.dse.geometry.Geometry;
import com.datastax.dse.graph.api.predicates.Geo;
import com.google.common.base.Preconditions;

import java.util.function.BiPredicate;

/**
 * List of predicates for geolocation usage with DseGraph and Search indexes.
 * Should not be accessed directly but through the {@link Geo} static methods.
 */
public enum GeoPredicate implements BiPredicate<Object, Object> {
    inside {
        public boolean test(Object value, Object condition) {
            Preconditions.checkArgument(condition instanceof Geometry);
            if (value == null) {
                return false;
            } else {
                Preconditions.checkArgument(value instanceof Geometry);
                return ((Geometry) condition).contains((Geometry) value);
            }
        }

        public String toString() {
            return "inside";
        }
    }
}
