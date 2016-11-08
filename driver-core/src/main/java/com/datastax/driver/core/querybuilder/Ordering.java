/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.querybuilder;

import com.datastax.driver.core.CodecRegistry;

import java.util.List;

public class Ordering extends Utils.Appendeable {

    private final String name;
    private final boolean isDesc;

    Ordering(String name, boolean isDesc) {
        this.name = name;
        this.isDesc = isDesc;
    }

    @Override
    void appendTo(StringBuilder sb, List<Object> variables, CodecRegistry codecRegistry) {
        Utils.appendName(name, sb);
        sb.append(isDesc ? " DESC" : " ASC");
    }

    @Override
    boolean containsBindMarker() {
        return false;
    }
}
