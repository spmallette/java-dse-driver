/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.geometry;

import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.ogc.OGCGeometry;
import com.esri.core.geometry.ogc.OGCPoint;

import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * The driver-side representation of DSE's {@code Geo.distance}.
 * <p/>
 * This is a circle in a two-dimensional XY plane represented by its center point and radius.  It is used as a search
 * criteria to determine whether or not another geospatial object lies within a circular area.
 * <p/>
 * Note that this shape has no equivalent in the OGC and GeoJSON standards: as a consequence, {@link #asWellKnownText()}
 * returns a custom format, and {@link #asWellKnownBinary()}, {@link #asGeoJson()} and {@link #asGeoJson()} are not
 * supported.
 */
public class Distance extends Geometry {

    private static final Pattern WKT_PATTERN = Pattern.compile("distance *\\( *\\( *([\\d\\.-]+) *([\\d+\\.-]+) *\\) *([\\d+\\.-]+) *\\)", CASE_INSENSITIVE);

    /**
     * Creates a distance from its <a href="https://en.wikipedia.org/wiki/Well-known_text">Well-known Text</a> (WKT)
     * representation.
     *
     * @param source the Well-known Text representation to parse.
     * @return the point represented by the WKT.
     * @throws InvalidTypeException if the string does not contain a valid Well-known Text representation.
     * @see Distance#asWellKnownText()
     */
    public static Distance fromWellKnownText(String source) {
        Matcher matcher = WKT_PATTERN.matcher(source.trim());
        if (matcher.matches() && matcher.groupCount() == 3) {
            try {
                return new Distance(new Point(Double.parseDouble(matcher.group(1)), Double.parseDouble(matcher.group(2))), Double.parseDouble(matcher.group(3)));
            } catch (NumberFormatException var3) {
                throw new InvalidTypeException(String.format("Unable to parse %s", source));
            }
        } else {
            throw new InvalidTypeException(String.format("Unable to parse %s", source));
        }
    }

    private final Point center;

    private final double radius;

    /**
     * Creates a new distance with the given center and radius.
     *
     * @param center The center point.
     * @param radius The radius of the circle representing distance.
     */
    public Distance(Point center, double radius) {
        super(center.getOgcGeometry());
        checkNotNull(center);
        checkNotNull(radius);
        this.center = center;
        this.radius = radius;
    }

    /**
     * @return The center point of the circle representing this distance.
     */
    public Point getCenter() {
        return center;
    }

    /**
     * @return The radius of the circle representing this distance.
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Returns a <a href="https://en.wikipedia.org/wiki/Well-known_text">Well-known Text</a> (WKT)
     * representation of this geospatial type.
     * <p>
     * Since there is no Well-known Text specification for Distance, this returns a custom format of:
     * <p>
     * <code>DISTANCE((center.x center.y) radius)</code>
     *
     * @return a Well-known Text representation of this object.
     */
    @Override
    public String asWellKnownText() {
        return String.format("DISTANCE((%s %s) %s)", this.center.X(), this.center.Y(), this.radius);
    }

    /**
     * The distance type has no equivalent in the OGC standard: this method throws an
     * {@link UnsupportedOperationException}.
     */
    @Override
    public OGCGeometry getOgcGeometry() {
        throw new UnsupportedOperationException();
    }

    /**
     * The distance type has no equivalent in the OGC standard: this method throws an
     * {@link UnsupportedOperationException}.
     */
    @Override
    public ByteBuffer asWellKnownBinary() {
        throw new UnsupportedOperationException();
    }

    /**
     * The distance type has no equivalent in the GeoJSON standard: this method throws an
     * {@link UnsupportedOperationException}.
     */
    @Override
    public String asGeoJson() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            Distance distance = (Distance) o;
            return Double.compare(distance.radius, this.radius) == 0 && this.center.equals(distance.center);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = this.center.hashCode();
        long temp = Double.doubleToLongBits(this.radius);
        result = 31 * result + (int) (temp ^ temp >>> 32);
        return result;
    }

    @SuppressWarnings("SimplifiableConditionalExpression")
    @Override
    public boolean contains(Geometry geometry) {
        return geometry instanceof Distance ?
                this.containsDistance((Distance) geometry) :
                geometry instanceof Point ?
                        this.containsPoint((Point) geometry) :
                        geometry instanceof LineString ?
                                this.containsLineString((LineString) geometry) :
                                geometry instanceof Polygon ?
                                        this.containsPolygon((Polygon) geometry) :
                                        false;
    }

    private boolean containsDistance(Distance distance) {
        return this.center.getOgcGeometry().distance(distance.center.getOgcGeometry()) + distance.radius <= this.radius;
    }

    private boolean containsPoint(Point point) {
        return this.containsOGCPoint(point.getOgcGeometry());
    }

    private boolean containsLineString(LineString lineString) {
        MultiPath multiPath = (MultiPath) lineString.getOgcGeometry().getEsriGeometry();
        return containsMultiPath(multiPath);
    }

    private boolean containsPolygon(Polygon polygon) {
        MultiPath multiPath = (com.esri.core.geometry.Polygon) polygon.getOgcGeometry().getEsriGeometry();
        return containsMultiPath(multiPath);
    }

    private boolean containsMultiPath(MultiPath multiPath) {
        int numPoints = multiPath.getPointCount();
        for (int i = 0; i < numPoints; ++i) {
            OGCPoint point = new OGCPoint(multiPath.getPoint(i), Geometry.SPATIAL_REFERENCE_4326);
            if (!this.containsOGCPoint(point)) {
                return false;
            }
        }
        return true;
    }

    private boolean containsOGCPoint(OGCPoint point) {
        return this.center.getOgcGeometry().distance(point) <= this.radius;
    }

}
