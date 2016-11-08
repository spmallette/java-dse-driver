/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.mapping.annotations;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.TypeCodec;

/**
 * Holds common defaults for the mapping annotations.
 */
public class Defaults {

    /**
     * A fake codec implementation to use as the default in mapping annotations.
     */
    public static abstract class NoCodec extends TypeCodec<String> {
        private NoCodec() {
            super(DataType.cint(), String.class);
        }
    }
}
