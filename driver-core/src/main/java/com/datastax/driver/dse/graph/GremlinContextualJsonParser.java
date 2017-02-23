/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.fasterxml.jackson.core.JsonParser;
import com.google.common.collect.Lists;

class GremlinContextualJsonParser extends ContextualDelegateParser {

    GremlinContextualJsonParser(JsonParser d, Element parent) {
        super(d, Lists.newArrayList((Object)parent));
    }

    Element getParent() {
        return (Element)getContext().get(0);
    }
}
