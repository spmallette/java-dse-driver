/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class SessionAssert extends AbstractAssert<SessionAssert, SessionManager> {

    protected SessionAssert(Session actual) {
        // We are cheating a bit by casting, but this is the only implementation anyway
        super((SessionManager) actual, SessionAssert.class);
    }

    public SessionAssert hasPoolFor(int hostNumber) {
        Host host = TestUtils.findHost(actual.cluster, hostNumber);
        assertThat(actual.pools.containsKey(host)).isTrue();
        return this;
    }

    public SessionAssert hasNoPoolFor(int hostNumber) {
        Host host = TestUtils.findHost(actual.cluster, hostNumber);
        assertThat(actual.pools.containsKey(host)).isFalse();
        return this;
    }
}
