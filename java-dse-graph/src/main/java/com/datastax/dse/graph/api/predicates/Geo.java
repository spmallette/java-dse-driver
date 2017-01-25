/*
 *      Copyright (C) 2012-2016 DataStax Inc.
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

    /**
     * Graph predicate for finding whether an entity is inside a defined distance.
     *
     * @param centerX the coordinate on the x-axis for the center of the distance.
     * @param centerY the coordinate of the y-axis for the center of the distance.
     * @param radius  the radius of the distance.
     * @return a predicate to use in TinkerPop on a graph data set.
     */
    public static P inside(double centerX, double centerY, double radius) {
        return inside(point(centerX, centerY), radius);
    }

    /**
     * Graph predicate for finding whether an entity is inside another Geo entity.
     *
     * @param center the center of the area to look into
     * @param radius the radius of the area to look into
     * @return a predicate to use in TinkerPop on a graph data set.
     */
    public static P inside(Point center, double radius) {
        return new P(GeoPredicate.inside, distance(center, radius));
    }

    /**
     * Graph predicate for finding whether an entity is inside another Geo entity.
     *
     * @param value the other Geo entity to check inside of.
     * @param <V>   the type of the Geo entity to check.
     * @return a predicate to use in TinkerPop on a graph data set.
     */
    public static <V> P<V> inside(V value) {
        return new P(GeoPredicate.inside, value);
    }

    /**
     * Create a Point object with the given x-axis and y-axis coordinates.
     *
     * @param x the x-axis coordinate.
     * @param y the y-axis coordinate.
     * @return the Point object.
     */
    public static Point point(double x, double y) {
        return new Point(x, y);
    }

    /**
     * Create a LineString object with the list of points given in parameter.
     *
     * @param points the points that define the linestring.
     * @return the LineString object.
     */
    public static LineString linestring(double... points) {
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
     * Create a Polygon with the points given in parameters.
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
