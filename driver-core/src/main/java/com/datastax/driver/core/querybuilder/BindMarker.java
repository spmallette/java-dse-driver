/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.querybuilder;

/**
 * A CQL3 bind marker.
 * <p/>
 * This can be either an anonymous bind marker or a named one (but note that
 * named ones are only supported starting in Cassandra 2.0.1).
 * <p/>
 * Please note that to create a new bind maker object you should use
 * {@link QueryBuilder#bindMarker()} (anonymous marker) or
 * {@link QueryBuilder#bindMarker(String)} (named marker).
 */
public class BindMarker {
    static final BindMarker ANONYMOUS = new BindMarker(null);

    private final String name;

    BindMarker(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        if (name == null)
            return "?";

        return Utils.appendName(name, new StringBuilder(name.length() + 1).append(':')).toString();
    }
}
