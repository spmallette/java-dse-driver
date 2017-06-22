/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.mapping;

import com.datastax.driver.core.CCMTestsSupport;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import org.testng.annotations.Test;

/**
 * Test for JAVA-1310 - validate ability to transient properties at manager and mapper levels
 */
public class MappingConfigurationTransientTest extends CCMTestsSupport {

    @Override
    public void onTestContextInitialized() {
        execute("CREATE TABLE foo (k int primary key, v int)");
        execute("INSERT INTO foo (k, v) VALUES (1, 1)");
    }

    @Test(groups = "short")
    public void should_ignore_property_if_field_annotated_transient() {
        // given default configuration
        MappingManager mappingManager = new MappingManager(session());
        // when creating a mapper
        // then mapping should succeed since non-existent column 'notAColumn' is annotated as Transient on its field
        mappingManager.mapper(Foo1.class);
    }

    @Table(name = "foo")
    @SuppressWarnings({"unused", "WeakerAccess"})
    public static class Foo1 {
        @PartitionKey
        private int k;
        @Transient
        private int notAColumn;
    }

    @Test(groups = "short")
    public void should_ignore_property_if_getter_annotated_transient() {
        // given default configuration
        MappingManager mappingManager = new MappingManager(session());
        // when creating a mapper
        // then mapping should succeed since non-existent column 'notAColumn' is annotated as @Transient on its getter
        mappingManager.mapper(Foo2.class);
    }

    @Table(name = "foo")
    @SuppressWarnings({"unused", "WeakerAccess"})
    public static class Foo2 {
        @PartitionKey
        private int k;
        private int notAColumn;

        @Transient
        public int getNotAColumn() {
            return notAColumn;
        }
    }

    @Table(name = "foo")
    @SuppressWarnings({"unused", "WeakerAccess"})
    public static class Foo3 {
        @PartitionKey
        private int k;
        private int notAColumn;
    }

    @Test(groups = "short")
    public void should_ignore_property_if_declared_transient_in_mapper_configuration() {
        // given configuration that considers 'notAColumn' as transient
        MappingConfiguration conf = MappingConfiguration.builder()
                .withPropertyMapper(new DefaultPropertyMapper()
                        .addTransientPropertyNames("notAColumn"))
                .build();
        // when creating a mapper
        // then mapping should succeed since non-existent column 'notAColumn' is considered implicitly transient
        MappingManager mappingManager = new MappingManager(session(), conf);
        mappingManager.mapper(Foo3.class);
    }

    @Test(groups = "short")
    public void should_ignore_property_if_field_has_transient_modifier() {
        // given default configuration
        MappingManager mappingManager = new MappingManager(session());
        // when creating a mapper
        // then mapping should succeed since non-existent column 'notAColumn' has transient modifier
        mappingManager.mapper(Foo4.class);
    }

    @Table(name = "foo")
    @SuppressWarnings({"unused", "WeakerAccess"})
    public static class Foo4 {
        @PartitionKey
        private int k;
        private transient int notAColumn;
    }
}
