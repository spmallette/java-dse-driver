/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.geometry;

import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.esri.core.geometry.GeometryException;
import com.esri.core.geometry.ogc.OGCGeometry;
import com.esri.core.geometry.ogc.OGCLineString;
import com.google.common.collect.ImmutableList;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Base class for geospatial types that have an
 * <a href="https://en.wikipedia.org/wiki/Open_Geospatial_Consortium">OGC</a> equivalent
 * within the <a href="http://esri.github.io/geometry-api-java/javadoc/com/esri/core/geometry/ogc/OGCGeometry.html">ESRI</a> library.
 *
 * @param <T> the equivalent OGC type, a concrete subtype of {@link OGCGeometry}
 */
abstract class OgcCompatibleGeometry<T extends OGCGeometry> extends Geometry {

    private static final long serialVersionUID = -3131471128157336656L;

    static <T extends OGCGeometry> T fromOgcWellKnownText(String source, Class<T> klass) {
        OGCGeometry geometry;
        try {
            geometry = OGCGeometry.fromText(source);
        } catch (IllegalArgumentException e) {
            throw new InvalidTypeException(e.getMessage());
        }
        validateType(geometry, klass);
        return klass.cast(geometry);
    }

    static <T extends OGCGeometry> T fromOgcWellKnownBinary(ByteBuffer source, Class<T> klass) {
        OGCGeometry geometry;
        try {
            geometry = OGCGeometry.fromBinary(source);
        } catch (IllegalArgumentException e) {
            throw new InvalidTypeException(e.getMessage());
        }
        validateType(geometry, klass);
        return klass.cast(geometry);
    }

    static <T extends OGCGeometry> T fromOgcGeoJson(String source, Class<T> klass) {
        OGCGeometry geometry;
        try {
            geometry = OGCGeometry.fromGeoJson(source);
        } catch (Exception e) {
            throw new InvalidTypeException(e.getMessage());
        }
        validateType(geometry, klass);
        return klass.cast(geometry);
    }

    private static void validateType(OGCGeometry geometry, Class<? extends OGCGeometry> klass) {
        if (!geometry.getClass().equals(klass)) {
            throw new InvalidTypeException(String.format("%s is not of type %s",
                    geometry.getClass().getSimpleName(),
                    klass.getSimpleName()));
        }
    }

    private final T ogcGeometry;

    protected OgcCompatibleGeometry(T ogcGeometry) {
        checkNotNull(ogcGeometry);
        validateOgcGeometry(ogcGeometry);
        this.ogcGeometry = ogcGeometry;
    }

    private static void validateOgcGeometry(OGCGeometry geometry) {
        try {
            if (geometry.is3D()) {
                throw new InvalidTypeException(String.format("'%s' is not 2D", geometry.asText()));
            }
            if (!geometry.isSimple()) {
                throw new InvalidTypeException(String.format("'%s' is not simple. Points and edges cannot self-intersect.", geometry.asText()));
            }
        } catch (GeometryException e) {
            throw new InvalidTypeException("Invalid geometry", e);
        }
    }

    static ImmutableList<Point> getPoints(OGCLineString lineString) {
        ImmutableList.Builder<Point> builder = ImmutableList.builder();
        for (int i = 0; i < lineString.numPoints(); i++) {
            builder.add(new Point(lineString.pointN(i)));
        }
        return builder.build();
    }

    /**
     * Returns the {@link OGCGeometry} object that this object maps to.
     *
     * @return an {@link OGCGeometry} object.
     */
    T getOgcGeometry() {
        return ogcGeometry;
    }

    com.esri.core.geometry.Geometry getEsriGeometry() {
        return ogcGeometry.getEsriGeometry();
    }

    @Override
    public String asWellKnownText() {
        return ogcGeometry.asText();
    }

    @Override
    public ByteBuffer asWellKnownBinary() {
        return ogcGeometry.asBinary();
    }

    @Override
    public String asGeoJson() {
        return ogcGeometry.asGeoJson();
    }

    @Override
    public boolean contains(Geometry other) {
        checkNotNull(other);

        if (!(other instanceof OgcCompatibleGeometry))
            throw new UnsupportedOperationException(String.format(
                    "Testing whether another geo type contains a %s is not supported",
                    other.getClass().getName()));

        return getOgcGeometry().contains(((OgcCompatibleGeometry) other).getOgcGeometry());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OgcCompatibleGeometry<?> that = (OgcCompatibleGeometry<?>) o;
        return this.getOgcGeometry().equals(that.getOgcGeometry());
    }

    @Override
    public int hashCode() {
        // OGCGeometry subclasses do not overwrite Object.hashCode()
        // while com.esri.core.geometry.Geometry subclasses usually do,
        // so use these instead; this is consistent with equals
        // because OGCGeometry.equals() actually compare between
        // com.esri.core.geometry.Geometry objects
        return getEsriGeometry().hashCode();
    }

    // Should never be called since we serialize a proxy (see subclasses)
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }
}
