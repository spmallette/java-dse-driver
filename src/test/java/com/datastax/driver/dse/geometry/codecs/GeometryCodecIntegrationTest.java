/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.geometry.codecs;

import com.datastax.driver.core.*;
import com.datastax.driver.core.utils.DseVersion;
import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.dse.CCMDseTestsSupport;
import com.datastax.driver.dse.geometry.Geometry;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import org.assertj.core.api.iterable.Extractor;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@DseVersion(major = 5.0)
public abstract class GeometryCodecIntegrationTest<T extends Geometry> extends CCMDseTestsSupport {

    private final String cqlTypeName;
    private final Class<T> selfType;
    private final T baseSample;
    private final List<T> sampleData;

    @SuppressWarnings("unchecked")
    public GeometryCodecIntegrationTest(String cqlTypeName, List<T> sampleData) {
        Preconditions.checkArgument(sampleData.size() >= 3, "Must be at least 3 samples, was given " + sampleData.size());
        this.cqlTypeName = cqlTypeName;
        this.baseSample = sampleData.get(0);
        this.selfType = (Class<T>) this.baseSample.getClass();
        this.sampleData = sampleData;
    }

    @DataProvider
    public Object[][] sampleProvider() {
        int count = sampleData.size();
        Iterator<T> iterator = sampleData.iterator();
        Object[][] data = new Object[count + 1][1];
        for (int i = 0; i < count && iterator.hasNext(); i++) {
            data[i][0] = iterator.next();
        }
        data[sampleData.size()][0] = null;
        return data;
    }

    @Override
    public void onTestContextInitialized() {
        execute(
                String.format("CREATE TYPE udt1 (g '%s')", cqlTypeName),
                String.format("CREATE TABLE tbl (k uuid PRIMARY KEY, g '%s', l list<'%s'>, s set<'%s'>, m0 map<'%s',int>, m1 map<int,'%s'>, t tuple<'%s','%s','%s'>, u frozen<udt1>)",
                        cqlTypeName, cqlTypeName, cqlTypeName, cqlTypeName, cqlTypeName, cqlTypeName, cqlTypeName, cqlTypeName),
                String.format("CREATE TABLE tblpk (k '%s' primary key, v int)", cqlTypeName),
                String.format("CREATE TABLE tblclustering (k0 int, k1 '%s', v int, primary key (k0, k1))", cqlTypeName)
        );
    }

    protected TypeCodec<T> codec() {
        return cluster().getConfiguration().getCodecRegistry().codecFor(sampleData.get(0));
    }

    protected <V> void validate(UUID key, String columnName, V expected, TypeToken<V> type) {
        Row row = session().execute(String.format("SELECT k,%s FROM tbl where k=?", columnName), key).one();
        assertThat(row.getUUID("k")).isEqualTo(key);
        assertThat(row.get(columnName, type)).isEqualTo(expected);
        assertThat(row.get(1, type)).isEqualTo(expected);
    }

    protected void validate(UUID key, T expected) {
        validate(key, "g", expected, TypeToken.of(selfType));
    }

    /**
     * Validates that a given geometry value can be inserted into a column using codec.format() and verifies that it
     * is stored correctly by retrieving it and ensuring it matches.
     *
     * @test_category dse:geospatial
     */
    @Test(groups = "short", dataProvider = "sampleProvider")
    public void should_insert_using_format(T expected) throws Exception {
        String val = null;
        if (expected != null) {
            TypeCodec<T> codec = cluster().getConfiguration().getCodecRegistry().codecFor(expected);
            val = codec.format(expected);
        }
        UUID key = UUIDs.random();
        session().execute(String.format("INSERT INTO tbl (k, g) VALUES (%s, %s)", key, val));
        validate(key, expected);
    }

    /**
     * Validates that a given geometry value can be inserted into a column by providing it as a simple statement
     * parameter and verifies that it is stored correctly by retrieving it and ensuring it matches.
     *
     * @test_category dse:geospatial
     */
    @Test(groups = "short", dataProvider = "sampleProvider")
    public void should_insert_using_simple_statement_with_parameters(T expected) throws Exception {
        UUID key = UUIDs.random();
        session().execute("INSERT INTO tbl (k, g) VALUES (?, ?)", key, expected);
        validate(key, expected);
    }

    /**
     * Validates that a given geometry value can be inserted into a column by providing it as a bound parameter
     * in a BoundStatement and verifies that it is stored correctly by retrieving it and ensuring it matches.
     *
     * @test_category dse:geospatial
     */
    @Test(groups = "short", dataProvider = "sampleProvider")
    public void should_insert_using_prepared_statement_with_parameters(T expected) throws Exception {
        UUID key = UUIDs.random();
        PreparedStatement prepared = session().prepare("INSERT INTO tbl (k, g) values (?, ?)");
        session().execute(prepared.bind(key, expected));
        validate(key, expected);
    }

    /**
     * Validates that geometry values can be inserted as a list and verifies that the list is stored correctly
     * by retrieving it and ensuring it matches.
     *
     * @test_category dse:geospatial
     */
    @Test(groups = "short")
    public void should_insert_as_list() throws Exception {
        UUID key = UUIDs.random();
        PreparedStatement prepared = session().prepare("INSERT INTO tbl (k, l) values (?, ?)");
        BoundStatement bs = prepared.bind();
        bs.setUUID(0, key);
        bs.setList(1, sampleData);
        session().execute(bs);
        validate(key, "l", sampleData, TypeTokens.listOf(selfType));
    }

    /**
     * Validates that geometry values can be inserted as a set and verifies that the set is stored correctly
     * by retrieving it and ensuring it matches.
     *
     * @test_category dse:geospatial
     */
    @Test(groups = "short")
    public void should_insert_as_set() throws Exception {
        UUID key = UUIDs.random();
        Set<T> asSet = Sets.newHashSet(sampleData);
        PreparedStatement prepared = session().prepare("INSERT INTO tbl (k, s) values (?, ?)");
        session().execute(prepared.bind(key, asSet));
        validate(key, "s", asSet, TypeTokens.setOf(selfType));
    }

    /**
     * Validates that geometry values can be inserted into a map as keys and verifies that the map is stored
     * correctly by retrieving it and ensuring it matches.
     *
     * @test_category dse:geospatial
     */
    @Test(groups = "short")
    public void should_insert_as_map_keys() throws Exception {
        UUID key = UUIDs.random();
        ImmutableMap.Builder<T, Integer> builder = ImmutableMap.builder();
        int count = 0;
        for (T val : sampleData) {
            builder = builder.put(val, count++);
        }
        Map<T, Integer> asMapKeys = builder.build();
        PreparedStatement prepared = session().prepare("INSERT INTO tbl (k, m0) values (?, ?)");
        session().execute(prepared.bind(key, asMapKeys));
        validate(key, "m0", asMapKeys, TypeTokens.mapOf(selfType, Integer.class));
    }

    /**
     * Validates that geometry values can be inserted into a map as values and verifies that the map is stored
     * correctly by retrieving it and ensuring it matches.
     *
     * @test_category dse:geospatial
     */
    @Test(groups = "short")
    public void should_insert_as_map_values() throws Exception {
        UUID key = UUIDs.random();
        ImmutableMap.Builder<Integer, T> builder = ImmutableMap.builder();
        int count = 0;
        for (T val : sampleData) {
            builder = builder.put(count++, val);
        }
        Map<Integer, T> asMapValues = builder.build();
        PreparedStatement prepared = session().prepare("INSERT INTO tbl (k, m1) values (?, ?)");
        session().execute(prepared.bind(key, asMapValues));
        validate(key, "m1", asMapValues, TypeTokens.mapOf(Integer.class, selfType));
    }

    /**
     * Validates that geometry values can be inserted as a tuple and verifies that the tuple is stored
     * correctly by retrieving it and ensuring it matches.
     *
     * @test_category dse:geospatial
     */
    @Test(groups = "short")
    public void should_insert_as_tuple() throws Exception {
        UUID key = UUIDs.random();
        TupleType tupleType = TupleType.of(ProtocolVersion.NEWEST_SUPPORTED,
                cluster().getConfiguration().getCodecRegistry(),
                codec().getCqlType(), codec().getCqlType(), codec().getCqlType());

        TupleValue tuple = tupleType.newValue(sampleData.get(0), sampleData.get(1), sampleData.get(2));
        PreparedStatement prepared = session().prepare("INSERT INTO tbl (k, t) values (?, ?)");
        session().execute(prepared.bind(key, tuple));

        Row row = session().execute("SELECT k,t FROM tbl where k=?", key).one();
        assertThat(row.getUUID("k")).isEqualTo(key);
        assertThat(row.getTupleValue("t")).isEqualTo(tuple);
        assertThat(row.getTupleValue(1)).isEqualTo(tuple);
    }

    /**
     * Validates that a geometry value can be inserted as a field in a UDT and verifies that the UDT is stored
     * correctly by retrieving it and ensuring it matches.
     *
     * @test_category dse:geospatial
     */
    @Test(groups = "short")
    public void should_insert_as_field_in_udt() throws Exception {
        UUID key = UUIDs.random();
        UserType udtType = cluster().getMetadata().getKeyspace(keyspace).getUserType("udt1");
        assertThat(udtType).isNotNull();

        UDTValue value = udtType.newValue();
        value.set("g", sampleData.get(0), selfType);

        PreparedStatement prepared = session().prepare("INSERT INTO tbl (k, u) values (?, ?)");
        session().execute(prepared.bind(key, value));

        Row row = session().execute("SELECT k,u FROM tbl where k=?", key).one();
        assertThat(row.getUUID("k")).isEqualTo(key);
        assertThat(row.getUDTValue("u")).isEqualTo(value);
        assertThat(row.getUDTValue(1)).isEqualTo(value);
    }

    /**
     * Validates that a geometry value can be inserted into a column that is the partition key and then validates
     * that it can be queried back by partition key.
     *
     * @test_category dse:geospatial
     */
    @Test(groups = "short")
    public void should_accept_as_partition_key() throws Exception {
        session().execute("INSERT INTO tblpk (k, v) VALUES (?,?)", baseSample, 1);
        ResultSet results = session().execute("SELECT k,v FROM tblpk");
        Row row = results.one();
        T key = row.get("k", selfType);
        assertThat(key).isEqualTo(baseSample);
    }

    /**
     * Validates that geometry values can be inserted into a column that is a clustering key in rows sharing a
     * partition key and then validates that the rows can be retrieved by partition key.
     *
     * @test_category dse:geospatial
     */
    @Test(groups = "short")
    public void should_accept_as_clustering_key() throws Exception {
        PreparedStatement insert = session().prepare("INSERT INTO tblclustering (k0, k1, v) values (?,?,?)");
        BatchStatement batchStatement = new BatchStatement();
        int count = 0;
        for (T value : sampleData) {
            batchStatement.add(insert.bind(0, value, count++));
        }
        session().execute(batchStatement);

        ResultSet result = session().execute("SELECT * from tblclustering where k0=?", 0);

        // The order of rows returned is not significant for geospatial types since it is stored in lexicographic
        // byte order (8 bytes at a time). Thus we pull them all sort and extract and ensure all values were returned.
        List<Row> rows = result.all();

        assertThat(rows).extracting(new Extractor<Row, T>() {
            @Override
            public T extract(Row row) {
                return row.get("k1", selfType);
            }
        }).containsOnlyElementsOf(sampleData).hasSameSizeAs(sampleData);
    }


}
