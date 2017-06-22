/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.geometry;

import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.ogc.OGCLineString;
import com.google.common.collect.ImmutableList;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * The driver-side representation for DSE's {@code LineStringType}.
 * <p/>
 * This is a curve in a two-dimensional XY-plane, represented by a set of points (with linear interpolation between
 * them).
 */
public class LineString extends Geometry {

    private static final long serialVersionUID = -2541987694856357606L;

    /**
     * Creates a line string from its <a href="https://en.wikipedia.org/wiki/Well-known_text">Well-known Text</a> (WKT)
     * representation.
     *
     * @param source the Well-known Text representation to parse.
     * @return the line string represented by the WKT.
     * @throws InvalidTypeException if the string does not contain a valid Well-known Text representation.
     */
    public static LineString fromWellKnownText(String source) {
        return new LineString(fromOgcWellKnownText(source, OGCLineString.class));
    }

    /**
     * Creates a line string from its
     * <a href="https://en.wikipedia.org/wiki/Well-known_text#Well-known_binary">Well-known Binary</a> (WKB)
     * representation.
     *
     * @param source the Well-known Binary representation to parse.
     * @return the line string represented by the WKB.
     * @throws InvalidTypeException if the provided {@link ByteBuffer} does not contain a valid Well-known Binary
     *                              representation.
     */
    public static LineString fromWellKnownBinary(ByteBuffer source) {
        return new LineString(fromOgcWellKnownBinary(source, OGCLineString.class));
    }

    /**
     * Creates a line string from its JSON representation.
     *
     * @param source the JSON representation to parse.
     * @return the line string represented by the JSON.
     * @throws InvalidTypeException if the string does not contain a valid JSON representation.
     */
    public static LineString fromGeoJson(String source) {
        return new LineString(fromOgcGeoJson(source, OGCLineString.class));
    }

    private static OGCLineString fromPoints(Point p1, Point p2, Point... pn) {
        Polyline polyline = new Polyline((com.esri.core.geometry.Point) p1.getEsriGeometry(),
                (com.esri.core.geometry.Point) p2.getEsriGeometry());
        for (Point p : pn) {
            polyline.lineTo((com.esri.core.geometry.Point) p.getEsriGeometry());
        }
        return new OGCLineString(polyline, 0, Geometry.SPATIAL_REFERENCE_4326);
    }

    private final List<Point> points;

    /**
     * Creates a line string from a series of 2 or more points.
     *
     * @param p1 the first point.
     * @param p2 the second point.
     * @param pn additional points.
     */
    public LineString(Point p1, Point p2, Point... pn) {
        super(fromPoints(p1, p2, pn));
        this.points = ImmutableList.<Point>builder().add(p1).add(p2).add(pn).build();
    }

    private LineString(OGCLineString lineString) {
        super(lineString);
        this.points = getPoints(lineString);
    }

    /**
     * Returns the points composing this line string.
     *
     * @return the points (as an immutable list).
     */
    public List<Point> getPoints() {
        return points;
    }

    /**
     * This object gets replaced by an internal proxy for serialization.
     *
     * @serialData a single byte array containing the Well-Known Binary representation.
     */
    private Object writeReplace() {
        return new WkbSerializationProxy(this.asWellKnownBinary());
    }
}
