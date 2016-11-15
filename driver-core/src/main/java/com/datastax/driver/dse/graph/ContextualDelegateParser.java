/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.JsonParserDelegate;

import java.util.List;

class ContextualDelegateParser extends JsonParserDelegate {

    List<Object> context;

    ContextualDelegateParser(JsonParser d, List<Object> context) {
        super(d);
        this.context = context;
    }

    public List<Object> getContext() {
        return context;
    }
}
