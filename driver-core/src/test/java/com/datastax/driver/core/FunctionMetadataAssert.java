/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class FunctionMetadataAssert extends AbstractAssert<FunctionMetadataAssert, FunctionMetadata> {
    protected FunctionMetadataAssert(FunctionMetadata actual) {
        super(actual, FunctionMetadataAssert.class);
    }

    public FunctionMetadataAssert hasSignature(String name) {
        assertThat(actual.getSignature()).isEqualTo(name);
        return this;
    }

    public FunctionMetadataAssert isInKeyspace(String keyspaceName) {
        assertThat(actual.getKeyspace().getName()).isEqualTo(keyspaceName);
        return this;
    }

    public FunctionMetadataAssert hasBody(String body) {
        assertThat(actual.getBody()).isEqualTo(body);
        return this;
    }
}
