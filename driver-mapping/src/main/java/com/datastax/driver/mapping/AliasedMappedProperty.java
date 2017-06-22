/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.mapping;

class AliasedMappedProperty {

    final MappedProperty<Object> mappedProperty;
    final String alias;

    @SuppressWarnings("unchecked")
    AliasedMappedProperty(MappedProperty<?> mappedProperty, String alias) {
        this.mappedProperty = (MappedProperty<Object>) mappedProperty;
        this.alias = alias;
    }
}
