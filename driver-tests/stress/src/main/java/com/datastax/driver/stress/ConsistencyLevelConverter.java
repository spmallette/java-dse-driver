/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.stress;

import com.datastax.driver.core.ConsistencyLevel;
import joptsimple.ValueConverter;

/**
 * Created by Alram.Lechner on 03.03.2016.
 */
public class ConsistencyLevelConverter implements ValueConverter<ConsistencyLevel> {

    @Override
    public ConsistencyLevel convert(String value) {
        return ConsistencyLevel.valueOf(value);
    }

    @Override
    public Class<ConsistencyLevel> valueType() {
        return ConsistencyLevel.class;
    }

    @Override
    public String valuePattern() {
        StringBuilder pattern = new StringBuilder();
        for (ConsistencyLevel level : ConsistencyLevel.values()) {
            pattern.append("(").append(level.name()).append(")|");
        }
        pattern.setLength(pattern.length() - 1);
        return pattern.toString();
    }
}
