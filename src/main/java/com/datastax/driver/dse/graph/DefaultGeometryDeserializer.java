/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.dse.geometry.Geometry;
import com.datastax.driver.dse.geometry.LineString;
import com.datastax.driver.dse.geometry.Point;
import com.datastax.driver.dse.geometry.Polygon;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

/**
 * Default deserializer used by the driver for geospatial types.
 * It deserializes such types into {@link Geometry} instances.
 * The actual subclass depends on the type being deserialized.
 */
class DefaultGeometryDeserializer<T extends Geometry> extends StdDeserializer<T> {

    private final Class<T> geometryClass;

    DefaultGeometryDeserializer(Class<T> geometryClass) {
        super(geometryClass);
        this.geometryClass = geometryClass;
    }

    @Override
    public T deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        JsonLocation currentLocation = parser.getCurrentLocation();
        String wkt = parser.readValueAs(String.class);
        Geometry geometry;
        if (wkt.startsWith("POINT"))
            geometry = Point.fromWellKnownText(wkt);
        else if (wkt.startsWith("LINESTRING"))
            geometry = LineString.fromWellKnownText(wkt);
        else if (wkt.startsWith("POLYGON"))
            geometry = Polygon.fromWellKnownText(wkt);
        else
            throw new JsonParseException("Unknown geometry type: " + wkt, currentLocation);
        return geometryClass.cast(geometry);
    }

}
