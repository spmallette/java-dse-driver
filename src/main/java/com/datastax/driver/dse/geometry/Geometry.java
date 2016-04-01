/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.geometry;

import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.datastax.driver.dse.DseCluster;
import com.esri.core.geometry.GeometryException;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.ogc.OGCGeometry;
import com.esri.core.geometry.ogc.OGCLineString;
import com.google.common.collect.ImmutableList;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The driver-side representation for a DSE geospatial type.
 * <p/>
 * {@link DseCluster.Builder} registers {@link com.datastax.driver.dse.geometry.codecs codecs} for those types, so that
 * they can be used transparently in queries:
 * <pre>
 *     Row row = dseSession.execute("SELECT coords FROM points_of_interest WHERE name = 'Eiffel Tower'").one();
 *     Point coords = row.get("coords", Point.class);
 *
 *     dseSession.execute("INSERT INTO points_of_interest (name, coords) VALUES (?, ?)",
 *             "Washington Monument", new Point(38.8895, 77.0352));
 * </pre>
 */
public abstract class Geometry implements Serializable {

    private static final long serialVersionUID = -3131471128157336656L;

    /**
     * Default spatial reference for Well Known Text / Well Known Binary.
     * <p/>
     * 4326 is the <a href="http://www.epsg.org/">EPSG</a> identifier of the
     * <a href="https://en.wikipedia.org/wiki/World_Geodetic_System">World Geodetic System (WGS)</a>
     * in its later revision, WGS 84.
     */
    static final SpatialReference SPATIAL_REFERENCE_4326 = SpatialReference.create(4326);

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

    private final OGCGeometry ogcGeometry;

    protected Geometry(OGCGeometry ogcGeometry) {
        this.ogcGeometry = ogcGeometry;
        checkNotNull(ogcGeometry);
        validateOgcGeometry(ogcGeometry);
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
    OGCGeometry getOgcGeometry() {
        return ogcGeometry;
    }

    com.esri.core.geometry.Geometry getEsriGeometry() {
        return ogcGeometry.getEsriGeometry();
    }

    /**
     * Returns a <a href="https://en.wikipedia.org/wiki/Well-known_text">Well-known Text</a> (WKT)
     * representation of this geospatial type.
     *
     * @return a Well-known Text representation of this object.
     */
    public String asWellKnownText() {
        return ogcGeometry.asText();
    }

    /**
     * Returns a <a href="https://en.wikipedia.org/wiki/Well-known_text#Well-known_binary">Well-known Binary</a> (WKB)
     * representation of this geospatial type.
     *
     * @return a Well-known Binary representation of this object.
     */
    public ByteBuffer asWellKnownBinary() {
        return ogcGeometry.asBinary();
    }

    /**
     * Returns a JSON representation of this geospatial type.
     *
     * @return a JSON representation of this object.
     */
    public String asGeoJson() {
        return ogcGeometry.asGeoJson();
    }

    /**
     * Tests whether this geospatial type instance contains another instance.
     *
     * @param other the other instance.
     * @return whether {@code this} contains {@code other}.
     */
    public boolean contains(Geometry other) {
        checkNotNull(other);
        return getOgcGeometry().contains(other.getOgcGeometry());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Geometry that = (Geometry) o;
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

    @Override
    public String toString() {
        return asWellKnownText();
    }
}
