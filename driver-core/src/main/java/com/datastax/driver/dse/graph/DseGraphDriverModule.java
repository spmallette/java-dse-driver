/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.dse.geometry.Distance;
import com.datastax.driver.dse.geometry.Geometry;
import com.datastax.driver.dse.geometry.LineString;
import com.datastax.driver.dse.geometry.Point;
import com.datastax.driver.dse.geometry.Polygon;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A Jackson Module to use for TinkerPop serialization/deserialization. It extends
 * {@link com.datastax.driver.dse.graph.GraphSON2JacksonModule} because of the specific typing format used in GraphSON.
 */
class DseGraphDriverModule extends GraphSON2JacksonModule {

    public DseGraphDriverModule() {
        super("graph-graphson2dsegraph");
        addSerializer(LineString.class, new LineStringGeometrySerializer());
        addSerializer(Distance.class, new DistanceGeometrySerializer());
        addSerializer(Point.class, new PointGeometrySerializer());
        addSerializer(Polygon.class, new PolygonGeometrySerializer());

        addDeserializer(LineString.class, new LineStringGeometryDeserializer());
        addDeserializer(Point.class, new PointGeometryDeserializer());
        addDeserializer(Polygon.class, new PolygonGeometryDeserializer());
        addDeserializer(Distance.class, new DistanceGeometryDeserializer());
    }

    @Override
    public Map<Class<?>, String> getTypeDefinitions() {
        Map<Class<?>, String> definitions = new HashMap<Class<?>, String>();
        definitions.put(LineString.class, "LineString");
        definitions.put(Point.class, "Point");
        definitions.put(Polygon.class, "Polygon");
        definitions.put(Distance.class, "Distance");
        definitions.put(byte[].class, "Blob");
        return definitions;
    }

    @Override
    public String getTypeNamespace() {
        return "dse";
    }

    abstract static class AbstractGeometryJacksonDeserializer<T extends Geometry> extends StdDeserializer<T> {
        public AbstractGeometryJacksonDeserializer(final Class<T> clazz) {
            super(clazz);
        }

        public abstract T parse(final String val);

        @Override
        public T deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException {
            return parse(jsonParser.getText());
        }
    }

    abstract static class AbstractGeometryJacksonSerializer<T extends Geometry> extends StdScalarSerializer<T> {

        public AbstractGeometryJacksonSerializer(final Class<T> clazz) {
            super(clazz);
        }

        @Override
        public void serialize(final T value, final JsonGenerator gen,
                              final SerializerProvider serializerProvider) throws IOException {
            gen.writeString(value.asWellKnownText());
        }
    }


    static class LineStringGeometrySerializer extends AbstractGeometryJacksonSerializer<LineString> {
        public LineStringGeometrySerializer() {
            super(LineString.class);
        }
    }

    static class LineStringGeometryDeserializer extends AbstractGeometryJacksonDeserializer<LineString> {
        public LineStringGeometryDeserializer() {
            super(LineString.class);
        }

        @Override
        public LineString parse(final String val) {
            return LineString.fromWellKnownText(val);
        }
    }

    static class PolygonGeometrySerializer extends AbstractGeometryJacksonSerializer<Polygon> {
        public PolygonGeometrySerializer() {
            super(Polygon.class);
        }
    }

    static class PolygonGeometryDeserializer extends AbstractGeometryJacksonDeserializer<Polygon> {
        public PolygonGeometryDeserializer() {
            super(Polygon.class);
        }

        @Override
        public Polygon parse(final String val) {
            return Polygon.fromWellKnownText(val);
        }
    }

    static class PointGeometrySerializer extends AbstractGeometryJacksonSerializer<Point> {
        public PointGeometrySerializer() {
            super(Point.class);
        }
    }

    static class PointGeometryDeserializer extends AbstractGeometryJacksonDeserializer<Point> {
        public PointGeometryDeserializer() {
            super(Point.class);
        }

        @Override
        public Point parse(final String val) {
            return Point.fromWellKnownText(val);
        }
    }

    static class DistanceGeometrySerializer extends AbstractGeometryJacksonSerializer<Distance> {
        public DistanceGeometrySerializer() {
            super(Distance.class);
        }
    }

    static class DistanceGeometryDeserializer extends AbstractGeometryJacksonDeserializer<Distance> {
        public DistanceGeometryDeserializer() {
            super(Distance.class);
        }

        @Override
        public Distance parse(final String val) {
            return Distance.fromWellKnownText(val);
        }
    }


}
