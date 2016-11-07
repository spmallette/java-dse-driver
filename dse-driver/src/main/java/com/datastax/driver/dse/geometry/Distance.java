/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.geometry;

import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.ogc.OGCPoint;

import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

public class Distance extends Point {

    private static final Pattern WKT_PATTERN = Pattern.compile("distance *\\( *\\( *([\\d\\.-]+) *([\\d+\\.-]+) *\\) *([\\d+\\.-]+) *\\)", CASE_INSENSITIVE);

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

    private final double radius;

    public Distance(Point center, double radius) {
        super(center.getOgcGeometry());
        this.radius = radius;
    }

    @Override
    public String asWellKnownText() {
        return String.format("DISTANCE((%s %s) %s)", this.getOgcGeometry().X(), this.getOgcGeometry().Y(), this.radius);
    }

    @Override
    public ByteBuffer asWellKnownBinary() {
        throw new UnsupportedOperationException();
    }

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
            return Double.compare(distance.radius, this.radius) == 0 && this.getOgcGeometry().equals(distance.getOgcGeometry());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = this.getOgcGeometry().hashCode();
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
        return this.getOgcGeometry().distance(distance.getOgcGeometry()) + distance.radius <= this.radius;
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
        return this.getOgcGeometry().distance(point) <= this.radius;
    }

}
