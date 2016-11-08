/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.schemabuilder;

import com.datastax.driver.core.DataType;
import org.testng.annotations.Test;

import static com.datastax.driver.core.schemabuilder.SchemaBuilder.*;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateTypeTest {

    @Test(groups = "unit")
    public void should_create_UDT() throws Exception {
        //When
        SchemaStatement statement = createType("ks", "myType")
                .addColumn("col1", DataType.text())
                .addColumn("col2", DataType.bigint());

        //Then
        assertThat(statement.getQueryString()).isEqualTo("\n\tCREATE TYPE ks.myType(\n\t\t" +
                "col1 text,\n\t\t" +
                "col2 bigint)");
    }

    @Test(groups = "unit")
    public void should_create_UDT_if_not_exists() throws Exception {
        //When
        SchemaStatement statement = createType("myType")
                .ifNotExists()
                .addColumn("col1", DataType.text())
                .addColumn("col2", DataType.bigint());

        //Then
        assertThat(statement.getQueryString()).isEqualTo("\n\tCREATE TYPE IF NOT EXISTS myType(\n\t\t" +
                "col1 text,\n\t\t" +
                "col2 bigint)");
    }

    @Test(groups = "unit")
    public void should_create_simple_UDT_column() throws Exception {
        //When
        SchemaStatement statement = createType("ks", "myType")
                .addColumn("col1", DataType.text())
                .addUDTColumn("my_udt", frozen("address"));

        //Then
        assertThat(statement.getQueryString()).isEqualTo("\n\tCREATE TYPE ks.myType(\n\t\t" +
                "col1 text,\n\t\t" +
                "my_udt frozen<address>)");
    }

    @Test(groups = "unit")
    public void should_create_list_UDT_column() throws Exception {
        //When
        SchemaStatement statement = createType("ks", "myType")
                .addColumn("col1", DataType.text())
                .addUDTListColumn("my_udt", frozen("address"));

        //Then
        assertThat(statement.getQueryString()).isEqualTo("\n\tCREATE TYPE ks.myType(\n\t\t" +
                "col1 text,\n\t\t" +
                "my_udt list<frozen<address>>)");
    }

    @Test(groups = "unit")
    public void should_create_set_UDT_column() throws Exception {
        //When
        SchemaStatement statement = createType("ks", "myType")
                .addColumn("col1", DataType.text())
                .addUDTSetColumn("my_udt", frozen("address"));

        //Then
        assertThat(statement.getQueryString()).isEqualTo("\n\tCREATE TYPE ks.myType(\n\t\t" +
                "col1 text,\n\t\t" +
                "my_udt set<frozen<address>>)");
    }

    @Test(groups = "unit")
    public void should_create_key_UDT_map_column() throws Exception {
        //When
        SchemaStatement statement = createType("ks", "myType")
                .addColumn("col1", DataType.text())
                .addUDTMapColumn("my_udt", frozen("address"), DataType.text());

        //Then
        assertThat(statement.getQueryString()).isEqualTo("\n\tCREATE TYPE ks.myType(\n\t\t" +
                "col1 text,\n\t\t" +
                "my_udt map<frozen<address>, text>)");
    }

    @Test(groups = "unit")
    public void should_create_value_UDT_map_column() throws Exception {
        //When
        SchemaStatement statement = createType("ks", "myType")
                .addColumn("col1", DataType.text())
                .addUDTMapColumn("my_udt", DataType.cint(), frozen("address"));

        //Then
        assertThat(statement.getQueryString()).isEqualTo("\n\tCREATE TYPE ks.myType(\n\t\t" +
                "col1 text,\n\t\t" +
                "my_udt map<int, frozen<address>>)");
    }

    @Test(groups = "unit")
    public void should_create_key_value_UDT_map_column() throws Exception {
        //When
        SchemaStatement statement = createType("ks", "myType")
                .addColumn("col1", DataType.text())
                .addUDTMapColumn("my_udt", frozen("coords"), frozen("address"));

        //Then
        assertThat(statement.getQueryString()).isEqualTo("\n\tCREATE TYPE ks.myType(\n\t\t" +
                "col1 text,\n\t\t" +
                "my_udt map<frozen<coords>, frozen<address>>)");
    }

    @Test(groups = "unit")
    public void should_create_column_with_manual_type() throws Exception {
        //When
        SchemaStatement statement = createType("ks", "myType")
                .addColumn("col1", DataType.text())
                .addUDTColumn("my_udt", udtLiteral("frozen<address>"));

        //Then
        assertThat(statement.getQueryString()).isEqualTo("\n\tCREATE TYPE ks.myType(\n\t\t" +
                "col1 text,\n\t\t" +
                "my_udt frozen<address>)");
    }
}