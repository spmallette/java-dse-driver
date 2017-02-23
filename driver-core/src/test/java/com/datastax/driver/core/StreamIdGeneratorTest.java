/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class StreamIdGeneratorTest {

    @Test(groups = "unit")
    public void SimpleGenIdTest() throws Exception {

        StreamIdGenerator generator = StreamIdGenerator.newInstance(ProtocolVersion.V2);

        assertEquals(generator.next(), 0);
        assertEquals(generator.next(), 64);
        generator.release(0);
        assertEquals(generator.next(), 0);
        assertEquals(generator.next(), 65);
        assertEquals(generator.next(), 1);
        generator.release(64);
        assertEquals(generator.next(), 64);
        assertEquals(generator.next(), 2);

        for (int i = 5; i < 128; i++)
            generator.next();

        generator.release(100);
        assertEquals(generator.next(), 100);

        assertEquals(generator.next(), -1);
    }
}
