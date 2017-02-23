/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.exceptions;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * An interface for exceptions that are able to report the address of the coordinator host
 * that was contacted.
 */
public interface CoordinatorException {

    /**
     * The coordinator host that was contacted.
     * <p/>
     * This is a shortcut for {@link InetSocketAddress#getAddress() getAddress().getAddress()}.
     *
     * @return The coordinator host that was contacted;
     * may be {@code null} if the coordinator is not known.
     */
    InetAddress getHost();

    /**
     * The full address of the coordinator host that was contacted.
     *
     * @return the full address of the coordinator host that was contacted;
     * may be {@code null} if the coordinator is not known.
     */
    InetSocketAddress getAddress();
}
