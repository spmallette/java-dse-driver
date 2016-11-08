/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyspaceMetadataAssert extends AbstractAssert<KeyspaceMetadataAssert, KeyspaceMetadata> {
    protected KeyspaceMetadataAssert(KeyspaceMetadata actual) {
        super(actual, KeyspaceMetadataAssert.class);
    }

    public KeyspaceMetadataAssert hasName(String name) {
        assertThat(actual.getName()).isEqualTo(name);
        return this;
    }

    public KeyspaceMetadataAssert isDurableWrites() {
        assertThat(actual.isDurableWrites()).isTrue();
        return this;
    }

    public KeyspaceMetadataAssert isNotDurableWrites() {
        assertThat(actual.isDurableWrites()).isFalse();
        return this;
    }
}
