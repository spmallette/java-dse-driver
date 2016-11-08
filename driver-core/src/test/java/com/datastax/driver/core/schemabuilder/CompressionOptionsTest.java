/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.schemabuilder;

import org.testng.annotations.Test;

import static com.datastax.driver.core.schemabuilder.SchemaBuilder.*;
import static org.assertj.core.api.Assertions.assertThat;

public class CompressionOptionsTest {

    @Test(groups = "unit")
    public void should_build_compressions_options_for_lz4() throws Exception {
        //When
        final String built = lz4().withChunkLengthInKb(128).withCRCCheckChance(0.6D).build();

        //Then
        assertThat(built).isEqualTo("{'sstable_compression' : 'LZ4Compressor', 'chunk_length_kb' : 128, 'crc_check_chance' : 0.6}");
    }

    @Test(groups = "unit")
    public void should_create_snappy_compressions_options() throws Exception {
        //When
        final String built = snappy().withChunkLengthInKb(128).withCRCCheckChance(0.6D).build();

        //Then
        assertThat(built).isEqualTo("{'sstable_compression' : 'SnappyCompressor', 'chunk_length_kb' : 128, 'crc_check_chance' : 0.6}");
    }

    @Test(groups = "unit")
    public void should_create_deflate_compressions_options() throws Exception {
        //When
        final String built = deflate().withChunkLengthInKb(128).withCRCCheckChance(0.6D).build();

        //Then
        assertThat(built).isEqualTo("{'sstable_compression' : 'DeflateCompressor', 'chunk_length_kb' : 128, 'crc_check_chance' : 0.6}");
    }

    @Test(groups = "unit")
    public void should_create_no_compressions_options() throws Exception {
        //When
        final String built = noCompression().withChunkLengthInKb(128).withCRCCheckChance(0.6D).build();

        //Then
        assertThat(built).isEqualTo("{'sstable_compression' : ''}");
    }
}
