/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.mapping;

class AliasedMappedProperty<T> {

    final MappedProperty<T> mappedProperty;
    final String alias;

    AliasedMappedProperty(MappedProperty<T> mappedProperty, String alias) {
        this.mappedProperty = mappedProperty;
        this.alias = alias;
    }
}
