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

import com.esri.core.geometry.SpatialReference;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Base class for all geospatial types handled by the driver.
 */
public abstract class Geometry implements Serializable {

    /**
     * Default spatial reference for Well Known Text / Well Known Binary.
     * <p/>
     * 4326 is the <a href="http://www.epsg.org/">EPSG</a> identifier of the
     * <a href="https://en.wikipedia.org/wiki/World_Geodetic_System">World Geodetic System (WGS)</a>
     * in its later revision, WGS 84.
     */
    public static final SpatialReference SPATIAL_REFERENCE_4326 = SpatialReference.create(4326);

    /**
     * Returns a <a href="https://en.wikipedia.org/wiki/Well-known_text">Well-known Text</a> (WKT)
     * representation of this geospatial type.
     *
     * @return A Well-known Text representation of this object.
     */
    public abstract String asWellKnownText();

    /**
     * Returns a <a href="https://en.wikipedia.org/wiki/Well-known_text#Well-known_binary">Well-known Binary</a> (WKB)
     * representation of this geospatial type.
     *
     * @return A Well-known Binary representation of this object.
     */
    public abstract ByteBuffer asWellKnownBinary();

    /**
     * Returns a JSON representation of this geospatial type.
     *
     * @return A JSON representation of this object.
     */
    public abstract String asGeoJson();

    @Override
    public String toString() {
        return asWellKnownText();
    }

}
