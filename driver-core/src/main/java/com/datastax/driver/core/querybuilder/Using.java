/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.querybuilder;

import com.datastax.driver.core.CodecRegistry;

import java.util.List;

public abstract class Using extends Utils.Appendeable {

    final String optionName;

    private Using(String optionName) {
        this.optionName = optionName;
    }

    static class WithValue extends Using {
        private final long value;

        WithValue(String optionName, long value) {
            super(optionName);
            this.value = value;
        }

        @Override
        void appendTo(StringBuilder sb, List<Object> variables, CodecRegistry codecRegistry) {
            sb.append(optionName).append(' ').append(value);
        }

        @Override
        boolean containsBindMarker() {
            return false;
        }
    }

    static class WithMarker extends Using {
        private final BindMarker marker;

        WithMarker(String optionName, BindMarker marker) {
            super(optionName);
            this.marker = marker;
        }

        @Override
        void appendTo(StringBuilder sb, List<Object> variables, CodecRegistry codecRegistry) {
            sb.append(optionName).append(' ').append(marker);
        }

        @Override
        boolean containsBindMarker() {
            return true;
        }
    }
}
