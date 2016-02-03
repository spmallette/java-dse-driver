/*
 *      Copyright (C) 2012-2015 DataStax Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.datastax.driver.dse.geometry;

import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.ogc.OGCLineString;
import com.google.common.collect.ImmutableList;

import java.nio.ByteBuffer;
import java.util.List;

public class LineString extends OgcCompatibleGeometry<OGCLineString> {

    private static final long serialVersionUID = -2541987694856357606L;

    /**
     * Creates a {@link LineString} instance from
     * a <a href="https://en.wikipedia.org/wiki/Well-known_text">Well-known Text</a> (WKT)
     * representation of a linestring.
     *
     * @param source the Well-known Text representation to parse.
     * @return A {@link LineString} object.
     * @throws InvalidTypeException if the string does not contain a valid Well-known Text representation.
     */
    public static LineString fromWellKnownText(String source) {
        return new LineString(fromOgcWellKnownText(source, OGCLineString.class));
    }

    /**
     * Creates a {@link LineString} instance from
     * a <a href="https://en.wikipedia.org/wiki/Well-known_text#Well-known_binary">Well-known Binary</a> (WKB)
     * representation of a linestring.
     *
     * @param source the Well-known Binary representation to parse.
     * @return A {@link LineString} object.
     * @throws InvalidTypeException if the provided {@link ByteBuffer} does not contain a valid Well-known Binary representation.
     */
    public static LineString fromWellKnownBinary(ByteBuffer source) {
        return new LineString(fromOgcWellKnownBinary(source, OGCLineString.class));
    }

    /**
     * Creates a {@link LineString} instance from a JSON representation of a linestring.
     *
     * @param source the JSON representation to parse.
     * @return A {@link LineString} object.
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
     * Creates a {@link LineString} instance from a series of 2 or more {@link Point}s.
     *
     * @param p1 The first point.
     * @param p2 The second point.
     * @param pn Additional points.
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
     * @return the points composing this instance.
     */
    public List<Point> getPoints() {
        return points;
    }

    private Object writeReplace() {
        return new WkbSerializationProxy(this.asWellKnownBinary());
    }
}
