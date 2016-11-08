/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.querybuilder;

import com.datastax.driver.core.CCMTestsSupport;
import com.datastax.driver.core.TupleType;
import com.datastax.driver.core.TupleValue;
import com.datastax.driver.core.utils.CassandraVersion;
import com.google.common.collect.ImmutableList;
import org.testng.annotations.Test;

import java.util.List;

import static com.datastax.driver.core.DataType.cint;
import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

@CassandraVersion(major = 2.1, minor = 3)
public class QueryBuilderTupleExecutionTest extends CCMTestsSupport {

    @Test(groups = "short")
    public void should_handle_tuple() throws Exception {
        String query = "INSERT INTO foo (k,x) VALUES (0,(1));";
        TupleType tupleType = cluster().getMetadata().newTupleType(cint());
        BuiltStatement insert = insertInto("foo").value("k", 0).value("x", tupleType.newValue(1));
        assertEquals(insert.toString(), query);
    }

    @SuppressWarnings("deprecation")
    @Test(groups = "short")
    public void should_handle_collections_of_tuples() {
        String query;
        BuiltStatement statement;
        query = "UPDATE foo SET l=[(1,2)] WHERE k=1;";
        TupleType tupleType = cluster().getMetadata().newTupleType(cint(), cint());
        List<TupleValue> list = ImmutableList.of(tupleType.newValue(1, 2));
        statement = update("foo").with(set("l", list)).where(eq("k", 1));
        assertThat(statement.toString()).isEqualTo(query);
    }

}
