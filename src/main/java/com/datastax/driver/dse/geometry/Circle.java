/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.geometry;

import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.datastax.driver.core.utils.Bytes;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The driver-side representation for DSE's {@code CircleType}.
 * <p/>
 * This is a circle in a two-dimensional XY-plane, represented by a point indicating its center, and a radius.
 * <p/>
 * Note that there is no official representation of a circle in the WKT and WKB standards, therefore
 * {@link #asWellKnownText()} and {@link #asWellKnownBinary()} use a custom format.
 */
public class Circle extends Geometry {

    private static final long serialVersionUID = -1047638382280311774L;

    private static final Pattern WKT_PATTERN = Pattern.compile(
            "CIRCLE *\\( *\\( *([\\d\\.-]+) *([\\d+\\.-]+) *\\) *([\\d+\\.-]+) *\\)",
            Pattern.CASE_INSENSITIVE);

    private static final String GEO_JSON_TYPE = "Circle";

    private static final int WKB_TYPE = 101;

    private static final int WKB_SIZE = 1 + 4 + (8 * 3);

    private static final String WKT_EXAMPLE = new Circle(1, 2, 3).asWellKnownText();

    private static final String JSON_EXAMPLE = new Circle(1, 2, 3).asGeoJson();

    //@formatter:off
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<Map<String, Object>>(){};
    //@formatter:on

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Creates a circle from its <a href="https://en.wikipedia.org/wiki/Well-known_text">Well-known Text</a> (WKT)
     * representation.
     * <p/>
     * Note: there is no official WKT representation of a circle.
     * This method uses the following unofficial syntax: {@code CIRCLE ((X Y) RADIUS)}
     * where {@code (X Y)} is a point representing the circle's center.
     *
     * @param source the Well-known Text representation to parse.
     * @return the circle represented by the WKT.
     * @throws InvalidTypeException if the string does not contain a valid Well-known Text representation.
     */
    public static Circle fromWellKnownText(String source) {
        Matcher matcher = WKT_PATTERN.matcher(source.trim());
        if (!matcher.matches() || matcher.groupCount() != 3) {
            throw new InvalidTypeException(String.format("Unable to parse %s. WKT format for Circle is %s", source, WKT_EXAMPLE));
        }
        try {
            return new Circle(
                    Double.parseDouble(matcher.group(1)),
                    Double.parseDouble(matcher.group(2)),
                    Double.parseDouble(matcher.group(3)));
        } catch (NumberFormatException e) {
            throw new InvalidTypeException(String.format("Unable to parse %s. WKT format for Circle is %s", source, WKT_EXAMPLE));
        }
    }

    /**
     * Creates a circle from its
     * <a href="https://en.wikipedia.org/wiki/Well-known_text#Well-known_binary">Well-known Binary</a> (WKB)
     * representation.
     * <p/>
     * Note: there is no official WKB representation of a circle.
     * This class uses the following byte structure:
     * <ol>
     * <li>byte 1: the byte order (0 for big endian, 1 for little endian)</li>
     * <li>bytes 2-5: an int representing the circle type ({@code 101})</li>
     * <li>bytes 6-13: a double representing the circle's center X coordinate</li>
     * <li>bytes 14-21: a double representing the circle's center Y coordinate</li>
     * <li>bytes 22-29: a double representing the circle's radius</li>
     * </ol>
     *
     * @param source the Well-known Binary representation to parse.
     * @return the circle represented by the WKB.
     * @throws InvalidTypeException if the provided {@link ByteBuffer} does not contain a valid Well-known Binary
     *                              representation.
     */
    public static Circle fromWellKnownBinary(ByteBuffer source) {
        try {
            ByteOrder order = source.get() == 1 ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
            source.order(order);
            int type = source.getInt();
            if (type != WKB_TYPE) {
                throw new InvalidTypeException(String.format("Unable to decode Circle type. Expected type %s, got %s", WKB_TYPE, type));
            }
            return new Circle(source.getDouble(), source.getDouble(), source.getDouble());
        } catch (BufferUnderflowException e) {
            String msg = String.format("Unable to decode Circle type due to buffer underflow. Expected %s bytes, got %s", WKB_SIZE, Bytes.toHexString(source));
            throw new InvalidTypeException(msg);
        }
    }

    /**
     * Creates a circle instance from its JSON representation.
     *
     * @param source the JSON representation to parse.
     * @return the circle represented by the JSON.
     * @throws InvalidTypeException if the string does not contain a valid JSON representation.
     */
    public static Circle fromGeoJson(String source) {
        try {
            Map<String, Object> map = MAPPER.readValue(source, MAP_TYPE_REFERENCE);
            if (map.size() != 3) {
                throw new IllegalArgumentException(String.format("Expected 3 map entries, got %s", map.size()));
            }
            Object type = map.get("type");
            if (!GEO_JSON_TYPE.equals(type)) {
                throw new IllegalArgumentException(String.format("Expected type '%s', got '%s'", GEO_JSON_TYPE, type));
            }
            List<?> coordinates = (List<?>) map.get("coordinates");
            if (coordinates == null || coordinates.size() != 2) {
                throw new IllegalArgumentException(String.format("Exactly 2 coordinates required. Got %s",
                        coordinates != null ? coordinates.size() : null));
            }
            Object radius = map.get("radius");
            return new Circle(objToDouble(coordinates.get(0)), objToDouble(coordinates.get(1)), objToDouble(radius));
        } catch (Exception e) {
            throw new InvalidTypeException(String.format("Unable to parse %s. JSON format for Circle is %s", source, JSON_EXAMPLE), e);
        }
    }

    private static double objToDouble(Object o) {
        if (o != null && o instanceof Number) {
            return ((Number) o).doubleValue();
        } else {
            throw new IllegalArgumentException(String.format("Expected number, got '%s'", o));
        }
    }

    private final Point center;

    private final double radius;

    public Circle(double x, double y, double radius) {
        this(new Point(x, y), radius);
    }

    /**
     * Creates a new instance.
     *
     * @param center the center.
     * @param radius the radius.
     */
    public Circle(Point center, double radius) {
        checkNotNull(center);
        checkArgument(radius > 0, "radius must be > 0");
        this.center = center;
        this.radius = radius;
    }

    /**
     * Returns the center of this circle.
     *
     * @return the center of this circle.
     */
    public Point getCenter() {
        return center;
    }

    /**
     * Returns the radius of this circle.
     *
     * @return the radius of this circle.
     */
    public double getRadius() {
        return radius;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Note: there is no official WKB representation of a circle.
     * This class uses the following byte structure:
     * <ol>
     * <li>byte 1: the byte order (0 for big endian, 1 for little endian)</li>
     * <li>bytes 2-5: an int representing the circle type ({@code 101})</li>
     * <li>bytes 6-13: a double representing the circle's center X coordinate</li>
     * <li>bytes 14-21: a double representing the circle's center Y coordinate</li>
     * <li>bytes 22-29: a double representing the circle's radius</li>
     * </ol>
     */
    @Override
    public ByteBuffer asWellKnownBinary() {
        ByteBuffer bb = ByteBuffer.allocate(WKB_SIZE).order(ByteOrder.nativeOrder());
        bb.put((byte) (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? 1 : 0)); // endian
        bb.putInt(WKB_TYPE);
        bb.putDouble(center.X());
        bb.putDouble(center.Y());
        bb.putDouble(radius);
        bb.flip();
        return bb;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Note: there is no official WKT representation of a circle.
     * This method uses the following unofficial syntax: {@code CIRCLE ((X Y) RADIUS)}
     * where {@code (X Y)} is a point representing the circle's center.
     */
    @Override
    public String asWellKnownText() {
        DecimalFormat df = new DecimalFormat("#.#");
        df.setMinimumFractionDigits(0);
        return String.format("CIRCLE ((%s %s) %s)", df.format(center.X()), df.format(center.Y()), df.format(radius));
    }

    @Override
    public String asGeoJson() {
        return String.format("{\"type\":\"Circle\",\"coordinates\":[%s,%s], \"radius\":%s}", center.X(), center.Y(), radius);
    }

    @Override
    public boolean contains(Geometry other) {
        checkNotNull(other);

        if (other instanceof Point)
            return contains((Point) other);

        if (other instanceof LineString)
            return contains((LineString) other);

        if (other instanceof Polygon)
            return contains((Polygon) other);

        if (other instanceof Circle)
            return contains((Circle) other);

        throw new IllegalArgumentException("Unexpected type: " + other.getClass().getName());
    }

    private boolean contains(Point point) {
        return distanceToCenter(point) <= radius;
    }

    private double distanceToCenter(Point point) {
        return center.getOgcGeometry().distance(point.getOgcGeometry());
    }

    private boolean contains(List<Point> points) {
        for (Point point : points) {
            if (!contains(point))
                return false;
        }
        return true;
    }

    private boolean contains(LineString lineString) {
        return contains(lineString.getPoints());
    }

    private boolean contains(Polygon polygon) {
        if (!contains(polygon.getExteriorRing()))
            return false;
        for (List<Point> ring : polygon.getInteriorRings()) {
            if (!contains(ring))
                return false;
        }
        return true;
    }

    private boolean contains(Circle other) {
        return distanceToCenter(other.center) + other.radius <= radius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Circle circle = (Circle) o;
        return Double.compare(circle.radius, radius) == 0 && center.equals(circle.center);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = center.hashCode();
        temp = Double.doubleToLongBits(radius);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * This object gets replaced by an internal proxy for serialization.
     *
     * @serialData a single byte array containing the Well-Known Binary representation.
     */
    private Object writeReplace() {
        return new WkbSerializationProxy(this.asWellKnownBinary());
    }

    // Should never be called since we serialize a proxy
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }
}
