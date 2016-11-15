/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.dse.geometry.Geometry;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Default serializer used by the driver for geospatial types.
 * It serializes {@link Geometry} instances into their Well-Known Text (WKT) equivalent.
 */
class DefaultGeometrySerializer extends StdSerializer<Geometry> {

    DefaultGeometrySerializer() {
        super(Geometry.class);
    }

    @Override
    public void serialize(Geometry value, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
        jsonGenerator.writeString(value.asWellKnownText());
    }

}
