/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class AggregateMetadataAssert extends AbstractAssert<AggregateMetadataAssert, AggregateMetadata> {
    protected AggregateMetadataAssert(AggregateMetadata actual) {
        super(actual, AggregateMetadataAssert.class);
    }

    public AggregateMetadataAssert hasSignature(String name) {
        assertThat(actual.getSignature()).isEqualTo(name);
        return this;
    }

    public AggregateMetadataAssert isInKeyspace(String keyspaceName) {
        assertThat(actual.getKeyspace().getName()).isEqualTo(keyspaceName);
        return this;
    }

    public AggregateMetadataAssert hasInitCond(Object initCond) {
        assertThat(actual.getInitCond()).isEqualTo(initCond);
        return this;
    }
}
