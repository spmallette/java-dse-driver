/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import org.assertj.core.api.AbstractAssert;

import static com.datastax.driver.core.Assertions.assertThat;

public class DataTypeAssert extends AbstractAssert<DataTypeAssert, DataType> {
    public DataTypeAssert(DataType actual) {
        super(actual, DataTypeAssert.class);
    }

    public DataTypeAssert hasName(DataType.Name name) {
        assertThat(actual.name).isEqualTo(name);
        return this;
    }

    public DataTypeAssert isUserType(String keyspaceName, String userTypeName) {
        assertThat(actual).isInstanceOf(UserType.class);
        UserType userType = (UserType) this.actual;
        assertThat(userType.getKeyspace()).isEqualTo(keyspaceName);
        assertThat(userType.getTypeName()).isEqualTo(userTypeName);
        return this;
    }

    public DataTypeAssert isShallowUserType(String keyspaceName, String userTypeName) {
        assertThat(actual).isInstanceOf(UserType.Shallow.class);
        UserType.Shallow shallow = (UserType.Shallow) actual;
        assertThat(shallow.keyspaceName).isEqualTo(keyspaceName);
        assertThat(shallow.typeName).isEqualTo(userTypeName);
        return this;
    }

    public DataTypeAssert isFrozen() {
        assertThat(actual.isFrozen()).isTrue();
        return this;
    }

    public DataTypeAssert isNotFrozen() {
        assertThat(actual.isFrozen()).isFalse();
        return this;
    }

    public DataTypeAssert hasTypeArgument(int position, DataType expected) {
        assertThat(actual.getTypeArguments().get(position)).isEqualTo(expected);
        return this;
    }

    public DataTypeAssert hasTypeArguments(DataType... expected) {
        assertThat(actual.getTypeArguments()).containsExactly(expected);
        return this;
    }

    public DataTypeAssert hasField(String name, DataType expected) {
        assertThat(actual).isInstanceOf(UserType.class);
        UserType userType = (UserType) this.actual;
        assertThat(userType.getFieldType(name)).isEqualTo(expected);
        return this;
    }

}
