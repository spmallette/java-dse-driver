/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.graph.api.predicates;

import com.datastax.driver.dse.geometry.Distance;
import com.datastax.driver.dse.geometry.LineString;
import com.datastax.driver.dse.geometry.Point;
import com.datastax.driver.dse.geometry.Polygon;
import com.datastax.dse.graph.internal.GeoPredicate;
import com.google.common.base.Preconditions;
import org.apache.tinkerpop.gremlin.process.traversal.P;

public class Geo {

    private static final double DEGREES_TO_RADIANS = Math.PI / 180;
    private static final double EARTH_MEAN_RADIUS_KM = 6371.0087714;
    private static final double DEG_TO_KM = DEGREES_TO_RADIANS * EARTH_MEAN_RADIUS_KM;
    private static final double KM_TO_DEG = 1 / DEG_TO_KM;
    private static final double KM_TO_MILES = 0.621371192;
    private static final double MILES_TO_KM = 1 / KM_TO_MILES;

    public enum Unit {

        MILES(MILES_TO_KM * KM_TO_DEG),
        KILOMETERS(KM_TO_DEG),
        METERS(KM_TO_DEG / 1000.0),
        DEGREES(1);

        private double multiplier;


        Unit(double multiplier) {
            this.multiplier = multiplier;
        }

        /**
         * Convert distance to degrees
         * (used internally only).
         *
         * @param distance the distance to convert.
         * @return the distance in degrees.
         */
        public double toDegrees(double distance) {
            return distance * multiplier;
        }
    }

    /**
     * Graph predicate to find whether an entity is inside a defined area.
     *
     * @param center the center of the area to look into.
     * @param radius the radius of the area to look into.
     * @param units  the units of the radius.
     * @return a predicate to use in TinkerPop on a graph data set.
     */
    public static P<Object> inside(Point center, double radius, Unit units) {
        return new P<>(GeoPredicate.inside, distance(center, units.toDegrees(radius)));
    }

    /**
     * Graph predicate to find whether an entity is inside a defined {@link com.datastax.driver.dse.geometry.Polygon}.
     *
     * @param polygon the polygon entity to check inside of.
     * @return a predicate to use in TinkerPop on a graph data set.
     */
    public static P<Object> inside(Polygon polygon) {
        return new P<>(GeoPredicate.inside, polygon);
    }

    /**
     * Create a {@link com.datastax.driver.dse.geometry.Point} object with the given x-axis and y-axis coordinates.
     *
     * @param x the x-axis coordinate.
     * @param y the y-axis coordinate.
     * @return the Point object.
     */
    public static Point point(double x, double y) {
        return new Point(x, y);
    }

    /**
     * Create a {@link com.datastax.driver.dse.geometry.LineString} object with the list of points given in parameter.
     *
     * @param points the points that define the linestring.
     * @return the LineString object.
     */
    public static LineString lineString(double... points) {
        if (points.length % 2 != 0) {
            throw new IllegalArgumentException("lineString() must be passed an even number of arguments");
        } else if (points.length <= 0) {
            throw new IllegalArgumentException("lineString() must be passed at least two arguments");
        } else {
            StringBuilder sb = new StringBuilder("LINESTRING(");

            for (int i = 0; i < points.length; i += 2) {
                if (i > 0) {
                    sb.append(", ");
                }

                sb.append(points[i]);
                sb.append(" ");
                sb.append(points[i + 1]);
            }

            sb.append(")");
            return LineString.fromWellKnownText(sb.toString());
        }
    }

    /**
     * Create a {@link com.datastax.driver.dse.geometry.Polygon} with the points given in parameters.
     *
     * @param points the points that define the polygon.
     * @return the Polygon object.
     */
    public static Polygon polygon(double... points) {
        if (points.length % 2 != 0) {
            throw new IllegalArgumentException("polygon() must be passed an even number of arguments");
        } else if (points.length <= 0) {
            throw new IllegalArgumentException("polygon() must be passed at least two arguments");
        } else {
            StringBuilder sb = new StringBuilder("POLYGON((");

            for (int i = 0; i < points.length; i += 2) {
                if (i > 0) {
                    sb.append(", ");
                }

                sb.append(points[i]);
                sb.append(" ");
                sb.append(points[i + 1]);
            }

            sb.append("))");
            return Polygon.fromWellKnownText(sb.toString());
        }
    }

    /**
     * Create a Distance object that represents a point and its area's radius.
     * (used internally only).
     *
     * @param center the center point.
     * @param radius the radius of the area.
     * @return the Distance object.
     */
    private static Distance distance(Point center, double radius) {
        Preconditions.checkArgument(center != null, "Invalid center point");
        Preconditions.checkArgument(radius >= 0.0D, "Invalid radius: %s", radius);
        return new Distance(center, radius);
    }

}
