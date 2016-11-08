/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.mapping;

import com.datastax.driver.core.ConsistencyLevel;

import java.util.ArrayList;
import java.util.List;

class EntityMapper<T> {

    private final Class<T> entityClass;
    final String keyspace;
    final String table;

    final ConsistencyLevel writeConsistency;
    final ConsistencyLevel readConsistency;

    final List<PropertyMapper> partitionKeys = new ArrayList<PropertyMapper>();
    final List<PropertyMapper> clusteringColumns = new ArrayList<PropertyMapper>();

    final List<PropertyMapper> allColumns = new ArrayList<PropertyMapper>();

    EntityMapper(Class<T> entityClass, String keyspace, String table, ConsistencyLevel writeConsistency, ConsistencyLevel readConsistency) {
        this.entityClass = entityClass;
        this.keyspace = keyspace;
        this.table = table;
        this.writeConsistency = writeConsistency;
        this.readConsistency = readConsistency;
    }

    int primaryKeySize() {
        return partitionKeys.size() + clusteringColumns.size();
    }

    PropertyMapper getPrimaryKeyColumn(int i) {
        return i < partitionKeys.size() ? partitionKeys.get(i) : clusteringColumns.get(i - partitionKeys.size());
    }

    void addColumns(List<PropertyMapper> pks, List<PropertyMapper> ccs, List<PropertyMapper> rgs) {
        partitionKeys.addAll(pks);
        clusteringColumns.addAll(ccs);
        allColumns.addAll(pks);
        allColumns.addAll(ccs);
        allColumns.addAll(rgs);
    }

    T newEntity() {
        return ReflectionUtils.newInstance(entityClass);
    }

}
