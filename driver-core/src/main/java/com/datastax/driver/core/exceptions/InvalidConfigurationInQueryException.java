/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.exceptions;

import java.net.InetSocketAddress;

/**
 * A specific invalid query exception that indicates that the query is invalid
 * because of some configuration problem.
 * <p/>
 * This is generally throw by query that manipulate the schema (CREATE and
 * ALTER) when the required configuration options are invalid.
 */
public class InvalidConfigurationInQueryException extends InvalidQueryException implements CoordinatorException {

    private static final long serialVersionUID = 0;

    public InvalidConfigurationInQueryException(InetSocketAddress address, String msg) {
        super(address, msg);
    }

    @Override
    public InvalidConfigurationInQueryException copy() {
        return new InvalidConfigurationInQueryException(getAddress(), getMessage());
    }
}
