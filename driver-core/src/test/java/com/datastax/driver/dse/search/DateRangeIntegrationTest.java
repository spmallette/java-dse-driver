/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.search;

import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.dse.CCMDseTestsSupport;
import com.datastax.driver.dse.search.DateRange.DateRangeBound.Precision;
import com.google.common.collect.Sets;
import org.assertj.core.api.iterable.Extractor;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@DseVersion(major = 5.1)
public class DateRangeIntegrationTest extends CCMDseTestsSupport {

    /**
     * Validates that data can be retrieved by primary key where it's primary key is a 'DateRangeType' column and that
     * the data returned properly parses into the expected {@link DateRange}.
     *
     * @jira_ticket JAVA-1319
     * @test_category data_types:primitive
     */
    @Test(groups = "short")
    public void should_use_date_range_as_primary_key() throws Exception {
        execute(
                "CREATE TABLE dateRangeIntegrationTest1 (k 'DateRangeType' PRIMARY KEY, v int)",
                "INSERT INTO dateRangeIntegrationTest1 (k, v) VALUES ('[2010-12-03 TO 2010-12-04]', 1)",
                "INSERT INTO dateRangeIntegrationTest1 (k, v) VALUES ('[2015-12-03T10:15:30.001Z TO 2016-01-01T00:05:11.967Z]', 2)");

        ResultSet results = session().execute("SELECT * FROM dateRangeIntegrationTest1");
        List<Row> rows = results.all();
        assertThat(rows.size()).isEqualTo(2);
        assertThat(rows.get(0).get("k", DateRange.class)).isEqualTo(DateRange.parse("[2010-12-03 TO 2010-12-04]"));
        assertThat(rows.get(1).get("k", DateRange.class)).isEqualTo(DateRange.parse("[2015-12-03T10:15:30.001Z TO 2016-01-01T00:05:11.967Z]"));

        results = session().execute("SELECT * FROM dateRangeIntegrationTest1 WHERE k = '[2015-12-03T10:15:30.001Z TO 2016-01-01T00:05:11.967]'");
        rows = results.all();
        assertThat(rows.size()).isEqualTo(1);
        assertThat(rows.get(0).getInt("v")).isEqualTo(2);

        results = session().execute("SELECT * FROM dateRangeIntegrationTest1");
        rows = results.all();
        assertThat(rows.size()).isEqualTo(2);
        assertThat(rows.get(1).get("k", DateRange.class)).isEqualTo(DateRange.parse("[2015-12-03T10:15:30.001Z TO 2016-01-01T00:05:11.967Z]"));
    }

    /**
     * Validates that a 'DateRangeType' column can take a variety of {@link DateRange} inputs:
     * <p>
     * <ol>
     * <li>Upper bound unbounded</li>
     * <li>Lower bound unbounded</li>
     * <li>Unbounded</li>
     * <li>Bounded</li>
     * <li>null</li>
     * <li>unset</li>
     * </ol>
     *
     * @jira_ticket JAVA-1319
     * @test_category data_types:primitive
     */
    @Test(groups = "short")
    public void should_store_date_range() throws Exception {
        execute(
                "CREATE TABLE dateRangeIntegrationTest2 (k int PRIMARY KEY, v 'DateRangeType')",
                "INSERT INTO dateRangeIntegrationTest2 (k, v) VALUES (1, '[2000-01-01T10:15:30.301Z TO *]')",
                "INSERT INTO dateRangeIntegrationTest2 (k, v) VALUES (2, '[2000-02 TO 2000-03]')",
                "INSERT INTO dateRangeIntegrationTest2 (k, v) VALUES (3, '[* TO 2020]')",
                "INSERT INTO dateRangeIntegrationTest2 (k, v) VALUES (4, null)",
                "INSERT INTO dateRangeIntegrationTest2 (k)    VALUES (5)",
                "INSERT INTO dateRangeIntegrationTest2 (k, v) VALUES (6, '*')");

        ResultSet results = session().execute("SELECT * FROM dateRangeIntegrationTest2");
        List<Row> rows = results.all();
        assertThat(rows).extracting(new Extractor<Row, DateRange>() {
            @Override
            public DateRange extract(Row input) {
                return input.get("v", DateRange.class);
            }
        }).containsOnly(
                DateRange.parse("[2000-01-01T10:15:30.301Z TO *]"),
                DateRange.parse("[2000-02 TO 2000-03]"),
                DateRange.parse("[* TO 2020]"),
                null,
                DateRange.parse("*")
        );
    }

    /**
     * Validates that if a provided {@link DateRange} for a 'DateRangeType' column has the bounds reversed (lower
     * bound is later than upper bound) that an {@link InvalidQueryException} is thrown.
     *
     * @jira_ticket JAVA-1319
     * @test_category data_types:primitive
     */
    @Test(groups = "short")
    public void should_disallow_invalid_order() throws Exception {
        session().execute("CREATE TABLE dateRangeIntegrationTest3 (k int PRIMARY KEY, v 'DateRangeType')");
        try {
            session().execute("INSERT INTO dateRangeIntegrationTest3 (k, v) VALUES (1, '[2020-01-01T10:15:30.009Z TO 2010-01-01T00:05:11.031Z]')");
            fail("Expected InvalidQueryException");
        } catch (InvalidQueryException e) {
            assertThat(e.getMessage())
                    .contains("Wrong order: 2020-01-01T10:15:30.009Z TO 2010-01-01T00:05:11.031Z")
                    .contains("Could not parse date range: [2020-01-01T10:15:30.009Z TO 2010-01-01T00:05:11.031Z]");
        }
    }

    /**
     * Validates that {@link DateRange} can be used in UDT and Tuple types.
     *
     * @jira_ticket JAVA-1319
     * @test_category data_types:tuples
     * @test_category data_types:udt
     */
    @Test(groups = "short")
    public void should_allow_date_range_in_udt_and_tuple() throws Exception {
        execute("CREATE TYPE IF NOT EXISTS test_udt (i int, range 'DateRangeType')",
                "CREATE TABLE dateRangeIntegrationTest4 (k int PRIMARY KEY, u test_udt, uf frozen<test_udt>, " +
                        "t tuple<'DateRangeType', int>, tf frozen<tuple<'DateRangeType', int>>)",
                "INSERT INTO dateRangeIntegrationTest4 (k, u, uf, t, tf) VALUES (" +
                        "1, " +
                        "{i: 10, range: '[2000-01-01T10:15:30.003Z TO 2020-01-01T10:15:30.001Z]'}, " +
                        "{i: 20, range: '[2000-01-01T10:15:30.003Z TO 2020-01-01T10:15:30.001Z]'}, " +
                        "('[2000-01-01T10:15:30.003Z TO 2020-01-01T10:15:30.001Z]', 30), " +
                        "('[2000-01-01T10:15:30.003Z TO 2020-01-01T10:15:30.001Z]', 40))");

        DateRange expected = DateRange.parse("[2000-01-01T10:15:30.003Z TO 2020-01-01T10:15:30.001Z]");
        ResultSet results = session().execute("SELECT * FROM dateRangeIntegrationTest4");
        List<Row> rows = results.all();
        assertThat(rows.size()).isEqualTo(1);

        UDTValue u = rows.get(0).get("u", UDTValue.class);
        DateRange dateRange = u.get("range", DateRange.class);
        assertThat(dateRange).isEqualTo(expected);
        assertThat(u.getInt("i")).isEqualTo(10);

        u = rows.get(0).get("uf", UDTValue.class);
        dateRange = u.get("range", DateRange.class);
        assertThat(dateRange).isEqualTo(expected);
        assertThat(u.getInt("i")).isEqualTo(20);

        TupleValue t = rows.get(0).get("t", TupleValue.class);
        dateRange = t.get(0, DateRange.class);
        assertThat(dateRange).isEqualTo(expected);
        assertThat(t.getInt(1)).isEqualTo(30);

        t = rows.get(0).get("tf", TupleValue.class);
        dateRange = t.get(0, DateRange.class);
        assertThat(dateRange).isEqualTo(expected);
        assertThat(t.getInt(1)).isEqualTo(40);
    }

    /**
     * Validates that {@link DateRange} can be used in Collection types (Map, Set, List).
     *
     * @jira_ticket JAVA-1319
     * @test_category data_types:collections
     */
    @Test(groups = "short")
    public void should_allow_date_range_in_collections() throws Exception {
        execute("CREATE TABLE dateRangeIntegrationTest5 (k int PRIMARY KEY, l list<'DateRangeType'>, " +
                        "s set<'DateRangeType'>, dr2i map<'DateRangeType', int>, i2dr map<int, 'DateRangeType'>)",
                "INSERT INTO dateRangeIntegrationTest5 (k, l, s, i2dr, dr2i) VALUES (" +
                        "1, " +
                        "['[2000-01-01T10:15:30.001Z TO 2020]', '[2010-01-01T10:15:30.001Z TO 2020]', '2001-01-02'], " +
                        "{'[2000-01-01T10:15:30.001Z TO 2020]', '[2000-01-01T10:15:30.001Z TO 2020]', '[2010-01-01T10:15:30.001Z TO 2020]'}, " +
                        "{1: '[2000-01-01T10:15:30.001Z TO 2020]', 2: '[2010-01-01T10:15:30.001Z TO 2020]'}, " +
                        "{'[2000-01-01T10:15:30.001Z TO 2020]': 1, '[2010-01-01T10:15:30.001Z TO 2020]': 2})");

        ResultSet results = session().execute("SELECT * FROM dateRangeIntegrationTest5");
        List<Row> rows = results.all();
        assertThat(rows.size()).isEqualTo(1);

        List<DateRange> drList = rows.get(0).getList("l", DateRange.class);
        assertThat(drList.size()).isEqualTo(3);
        assertThat(drList.get(0)).isEqualTo(DateRange.parse("[2000-01-01T10:15:30.001Z TO 2020]"));
        assertThat(drList.get(1)).isEqualTo(DateRange.parse("[2010-01-01T10:15:30.001Z TO 2020]"));
        assertThat(drList.get(2)).isEqualTo(DateRange.parse("2001-01-02"));

        Set<DateRange> drSet = rows.get(0).getSet("s", DateRange.class);
        assertThat(drSet.size()).isEqualTo(2);
        assertThat(drSet).isEqualTo(Sets.newHashSet(
                DateRange.parse("[2000-01-01T10:15:30.001Z TO 2020]"),
                DateRange.parse("[2010-01-01T10:15:30.001Z TO 2020]")));

        Map<DateRange, Integer> dr2i = rows.get(0).getMap("dr2i", DateRange.class, Integer.class);
        assertThat(dr2i.size()).isEqualTo(2);
        assertThat((int) dr2i.get(DateRange.parse("[2000-01-01T10:15:30.001Z TO 2020]"))).isEqualTo(1);
        assertThat((int) dr2i.get(DateRange.parse("[2010-01-01T10:15:30.001Z TO 2020]"))).isEqualTo(2);

        Map<Integer, DateRange> i2dr = rows.get(0).getMap("i2dr", Integer.class, DateRange.class);
        assertThat(i2dr.size()).isEqualTo(2);
        assertThat(i2dr.get(1)).isEqualTo(DateRange.parse("[2000-01-01T10:15:30.001Z TO 2020]"));
        assertThat(i2dr.get(2)).isEqualTo(DateRange.parse("[2010-01-01T10:15:30.001Z TO 2020]"));
    }

    /**
     * Validates that a 'DateRangeType' column can take a {@link DateRange} inputs as a prepared statement parameter.
     *
     * @jira_ticket JAVA-1319
     * @test_category data_types:primitive
     * @test_category prepared_statements:binding
     */
    @Test(groups = "short")
    public void should_allow_date_range_in_prepared_statement_parameter() throws Exception {
        session().execute("CREATE TABLE dateRangeIntegrationTest6 (k int PRIMARY KEY, v 'DateRangeType')");
        PreparedStatement statement = session().prepare("INSERT INTO dateRangeIntegrationTest6 (k,v) VALUES(?,?)");

        DateRange expected = DateRange.parse("[2007-12-03 TO 2007-12]");
        session().execute(statement.bind(1, expected));
        ResultSet results = session().execute("SELECT * FROM dateRangeIntegrationTest6");
        List<Row> rows = results.all();
        assertThat(rows.size()).isEqualTo(1);
        DateRange actual = rows.get(0).get("v", DateRange.class);
        assertThat(actual).isEqualTo(expected);
        assertThat(actual.getLowerBound().getPrecision()).isEqualTo(Precision.DAY);
        assertThat(actual.getUpperBound().getPrecision()).isEqualTo(Precision.MONTH);
        assertThat(actual.toString()).isEqualTo("[2007-12-03 TO 2007-12]");

        expected = DateRange.parse("[* TO *]");
        session().execute(statement.bind(1, expected));
        results = session().execute("SELECT * FROM dateRangeIntegrationTest6");
        rows = results.all();
        assertThat(rows.size()).isEqualTo(1);
        actual = rows.get(0).get("v", DateRange.class);
        assertThat(actual).isEqualTo(expected);
        assertThat(actual.getLowerBound().isUnbounded()).isTrue();
        assertThat(actual.isSingleBounded()).isFalse();
        assertThat(actual.getUpperBound().isUnbounded()).isTrue();
        assertThat(actual.toString()).isEqualTo("[* TO *]");

        expected = DateRange.parse("*");
        session().execute(statement.bind(1, expected));
        results = session().execute("SELECT * FROM dateRangeIntegrationTest6");
        rows = results.all();
        assertThat(rows.size()).isEqualTo(1);
        actual = rows.get(0).get("v", DateRange.class);
        assertThat(actual).isEqualTo(expected);
        assertThat(actual.getLowerBound().isUnbounded()).isTrue();
        assertThat(actual.isSingleBounded()).isTrue();
        assertThat(actual.toString()).isEqualTo("*");
    }

    /**
     * Validates that 'DateRangeType' columns are retrievable using <code>SELECT JSON</code> queries and that their
     * value representations match their input.
     *
     * @jira_ticket JAVA-1319
     * @test_category data_types:primitive
     * @test_category data_types:json
     */
    @Test(groups = "short")
    public void should_select_date_range_using_json() throws Exception {
        session().execute("CREATE TABLE dateRangeIntegrationTest7 (k int PRIMARY KEY, v 'DateRangeType')");
        PreparedStatement statement = session().prepare("INSERT INTO dateRangeIntegrationTest7 (k,v) VALUES(?,?)");

        DateRange expected = DateRange.parse("[2007-12-03 TO 2007-12]");
        session().execute(statement.bind(1, expected));
        ResultSet results = session().execute("SELECT JSON * FROM dateRangeIntegrationTest7");
        List<Row> rows = results.all();
        assertThat(rows.get(0).getString(0)).isEqualTo("{\"k\": 1, \"v\": \"[2007-12-03 TO 2007-12]\"}");

        expected = DateRange.parse("[* TO *]");
        session().execute(statement.bind(1, expected));
        results = session().execute("SELECT JSON * FROM dateRangeIntegrationTest7");
        rows = results.all();
        assertThat(rows.get(0).getString(0)).isEqualTo("{\"k\": 1, \"v\": \"[* TO *]\"}");

        expected = DateRange.parse("*");
        session().execute(statement.bind(1, expected));
        results = session().execute("SELECT JSON * FROM dateRangeIntegrationTest7");
        rows = results.all();
        assertThat(rows.get(0).getString(0)).isEqualTo("{\"k\": 1, \"v\": \"*\"}");
    }
    
}
