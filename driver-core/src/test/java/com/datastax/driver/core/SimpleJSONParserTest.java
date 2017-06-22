/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class SimpleJSONParserTest {

    @Test(groups = "unit")
    public void SimpleParsingTest() throws Exception {

        assertEquals(ImmutableList.of("1", "2", "3"), SimpleJSONParser.parseStringList("[\"1\",\"2\",\"3\"]"));
        assertEquals(ImmutableList.of("foo ' bar \""), SimpleJSONParser.parseStringList("[\"foo ' bar \\\"\"]"));

        assertEquals(ImmutableMap.of("foo", "bar", "bar", "foo"), SimpleJSONParser.parseStringMap("{\"foo\":\"bar\",\"bar\":\"foo\"}"));
    }
}

