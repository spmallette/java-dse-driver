/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.geometry;

import com.datastax.driver.dse.DseCluster;
import com.esri.core.geometry.SpatialReference;

import java.io.Serializable;
import java.nio.ByteBuffer;

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

    /**
     * Default spatial reference for Well Known Text / Well Known Binary.
     * <p/>
     * 4326 is the <a href="http://www.epsg.org/">EPSG</a> identifier of the
     * <a href="https://en.wikipedia.org/wiki/World_Geodetic_System">World Geodetic System (WGS)</a>
     * in its later revision, WGS 84.
     */
    static final SpatialReference SPATIAL_REFERENCE_4326 = SpatialReference.create(4326);

    /**
     * Returns a <a href="https://en.wikipedia.org/wiki/Well-known_text">Well-known Text</a> (WKT)
     * representation of this geospatial type.
     *
     * @return a Well-known Text representation of this object.
     */
    public abstract String asWellKnownText();

    /**
     * Returns a <a href="https://en.wikipedia.org/wiki/Well-known_text#Well-known_binary">Well-known Binary</a> (WKB)
     * representation of this geospatial type.
     *
     * @return a Well-known Binary representation of this object.
     */
    public abstract ByteBuffer asWellKnownBinary();

    /**
     * Returns a JSON representation of this geospatial type.
     *
     * @return a JSON representation of this object.
     */
    public abstract String asGeoJson();

    /**
     * Tests whether this geospatial type instance contains another instance.
     * <p/>
     * Note that testing for inclusion of a {@link Circle} into another type is not supported.
     *
     * @param other the other instance.
     * @return whether {@code this} contains {@code other}.
     * @throws UnsupportedOperationException if {@code other} is a {@link Circle} and {@code this} is another type.
     */
    public abstract boolean contains(Geometry other);

    @Override
    public String toString() {
        return asWellKnownText();
    }

}
