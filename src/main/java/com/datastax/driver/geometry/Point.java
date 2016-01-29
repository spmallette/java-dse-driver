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
package com.datastax.driver.geometry;

import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.esri.core.geometry.ogc.OGCPoint;

import java.nio.ByteBuffer;

/**
 * A Point is a zero-dimensional object that represents a specific (X,Y)
 * location in a two-dimensional XY-Plane. In case of Geographic Coordinate
 * Systems, the X coordinate is the longitude and the Y is the latitude.
 */
public class Point extends OgcCompatibleGeometry<OGCPoint> {

    private static final long serialVersionUID = 6329957740309318716L;

    /**
     * Creates a {@link Point} instance from
     * a <a href="https://en.wikipedia.org/wiki/Well-known_text">Well-known Text</a> (WKT)
     * representation of a point.
     *
     * @param source the Well-known Text representation to parse.
     * @return A {@link Point} object.
     * @throws InvalidTypeException if the string does not contain a valid Well-known Text representation.
     */
    public static Point fromWellKnownText(String source) {
        return new Point(fromOgcWellKnownText(source, OGCPoint.class));
    }

    /**
     * Creates a {@link Point} instance from
     * a <a href="https://en.wikipedia.org/wiki/Well-known_text#Well-known_binary">Well-known Binary</a> (WKB)
     * representation of a point.
     *
     * @param source the Well-known Binary representation to parse.
     * @return A {@link Point} object.
     * @throws InvalidTypeException if the provided {@link ByteBuffer} does not contain a valid Well-known Binary representation.
     */
    public static Point fromWellKnownBinary(ByteBuffer source) {
        return new Point(fromOgcWellKnownBinary(source, OGCPoint.class));
    }

    /**
     * Creates a {@link Point} instance from a JSON representation of a point.
     *
     * @param source the JSON representation to parse.
     * @return A {@link Point} object.
     * @throws InvalidTypeException if the string does not contain a valid JSON representation.
     */
    public static Point fromGeoJson(String source) {
        return new Point(fromOgcGeoJson(source, OGCPoint.class));
    }

    /**
     * Creates a new instance.
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     */
    public Point(double x, double y) {
        this(new OGCPoint(new com.esri.core.geometry.Point(x, y), Geometry.SPATIAL_REFERENCE_4326));
    }

    Point(OGCPoint point) {
        super(point);
    }

    /**
     * @return The X coordinate of this 2D point.
     */
    public double X() {
        return getOgcGeometry().X();
    }

    /**
     * @return The Y coordinate of this 2D point.
     */
    public double Y() {
        return getOgcGeometry().Y();
    }
}
