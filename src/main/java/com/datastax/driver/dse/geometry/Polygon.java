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
import com.esri.core.geometry.Operator;
import com.esri.core.geometry.OperatorFactoryLocal;
import com.esri.core.geometry.OperatorSimplifyOGC;
import com.esri.core.geometry.ogc.OGCPolygon;
import com.google.common.collect.ImmutableList;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

public class Polygon extends OgcCompatibleGeometry<OGCPolygon> {

    private static final long serialVersionUID = 7308381476240075319L;

    /**
     * Creates a {@link Polygon} instance from
     * a <a href="https://en.wikipedia.org/wiki/Well-known_text">Well-known Text</a> (WKT)
     * representation of a polygon.
     *
     * @param source the Well-known Text representation to parse.
     * @return A {@link Polygon} object.
     * @throws InvalidTypeException if the string does not contain a valid Well-known Text representation.
     */
    public static Polygon fromWellKnownText(String source) {
        return new Polygon(fromOgcWellKnownText(source, OGCPolygon.class));
    }

    /**
     * Creates a {@link Polygon} instance from
     * a <a href="https://en.wikipedia.org/wiki/Well-known_text#Well-known_binary">Well-known Binary</a> (WKB)
     * representation of a polygon.
     *
     * @param source the Well-known Binary representation to parse.
     * @return A {@link Polygon} object.
     * @throws InvalidTypeException if the provided {@link ByteBuffer} does not contain a valid Well-known Binary representation.
     */
    public static Polygon fromWellKnownBinary(ByteBuffer source) {
        return new Polygon(fromOgcWellKnownBinary(source, OGCPolygon.class));
    }

    /**
     * Creates a {@link Polygon} instance from a JSON representation of a polygon.
     *
     * @param source the JSON representation to parse.
     * @return A {@link Polygon} object.
     * @throws InvalidTypeException if the string does not contain a valid JSON representation.
     */
    public static Polygon fromGeoJson(String source) {
        return new Polygon(fromOgcGeoJson(source, OGCPolygon.class));
    }

    private final List<Point> exteriorRing;
    private final List<List<Point>> interiorRings;

    /**
     * Creates a {@link Polygon} instance from a series of 3 or more {@link Point}s.
     *
     * @param p1 The first point.
     * @param p2 The second point.
     * @param p3 The third point.
     * @param pn Additional points.
     */
    public Polygon(Point p1, Point p2, Point p3, Point... pn) {
        super(fromPoints(p1, p2, p3, pn));
        this.exteriorRing = ImmutableList.<Point>builder().add(p1).add(p2).add(p3).add(pn).build();
        this.interiorRings = Collections.emptyList();
    }

    private Polygon(OGCPolygon polygon) {
        super(polygon);
        this.exteriorRing = getPoints(polygon.exteriorRing());

        ImmutableList.Builder<List<Point>> builder = ImmutableList.builder();
        for (int i = 0; i < polygon.numInteriorRing(); i++) {
            builder.add(getPoints(polygon.interiorRingN(i)));
        }
        this.interiorRings = builder.build();
    }

    /**
     * @return the external ring of the polygon.
     */
    public List<Point> getExteriorRing() {
        return exteriorRing;
    }

    /**
     * @return the internal rings of the polygon, i.e. any holes inside of it (or island inside of the holes).
     */
    public List<List<Point>> getInteriorRings() {
        return interiorRings;
    }

    /**
     * Returns a polygon builder.
     * <p/>
     * This is intended for complex polygons with multiple rings (i.e. holes inside the polygon). For simple case,
     * consider the class's constructors.
     *
     * @return the builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Provides a DSL to build a polygon.
     */
    public static class Builder {
        private final com.esri.core.geometry.Polygon polygon = new com.esri.core.geometry.Polygon();

        /**
         * Adds a new ring for this polygon.
         * <p/>
         * There can be one or more outer rings and zero or more inner rings. If a polygon has an inner ring, the inner
         * ring looks like a hole. If the hole contains another outer ring, that outer ring looks like an island.
         * <p/>
         * There must be one "main" outer ring that contains all the others.
         *
         * @param p1 the first point.
         * @param p2 the second point.
         * @param p3 the third point.
         * @param pn additional points.
         * @return this builder.
         */
        public Builder addRing(Point p1, Point p2, Point p3, Point... pn) {
            addPath(polygon, p1, p2, p3, pn);
            return this;
        }

        /**
         * Builds the polygon.
         *
         * @return the polygon.
         */
        public Polygon build() {
            return new Polygon(new OGCPolygon(simplify(polygon), Geometry.SPATIAL_REFERENCE_4326));
        }
    }

    private static OGCPolygon fromPoints(Point p1, Point p2, Point p3, Point... pn) {
        com.esri.core.geometry.Polygon polygon = new com.esri.core.geometry.Polygon();
        addPath(polygon, p1, p2, p3, pn);
        return new OGCPolygon(simplify(polygon), Geometry.SPATIAL_REFERENCE_4326);
    }

    private static void addPath(com.esri.core.geometry.Polygon polygon, Point p1, Point p2, Point p3, Point[] pn) {
        polygon.startPath((com.esri.core.geometry.Point) p1.getEsriGeometry());
        polygon.lineTo((com.esri.core.geometry.Point) p2.getEsriGeometry());
        polygon.lineTo((com.esri.core.geometry.Point) p3.getEsriGeometry());
        for (Point p : pn) {
            polygon.lineTo((com.esri.core.geometry.Point) p.getEsriGeometry());
        }
    }

    private static com.esri.core.geometry.Polygon simplify(com.esri.core.geometry.Polygon polygon) {
        OperatorSimplifyOGC op = (OperatorSimplifyOGC) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.SimplifyOGC);
        return (com.esri.core.geometry.Polygon) op.execute(polygon, Geometry.SPATIAL_REFERENCE_4326, true, null);
    }
}
