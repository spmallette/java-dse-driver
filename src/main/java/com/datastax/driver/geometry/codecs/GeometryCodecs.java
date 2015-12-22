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
package com.datastax.driver.geometry.codecs;

import com.datastax.driver.core.TypeCodec;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Utilities related to geometry type codecs.
 */
public class GeometryCodecs {

    /**
     * The codecs for all geometry types. This is provided as a convenience to register them all in one call:
     * <pre>
     * codecRegistry.register(GeometryCodecs.ALL);
     * </pre>
     */
    public static List<TypeCodec<?>> ALL = ImmutableList.<TypeCodec<?>>builder()
            .add(CircleCodec.INSTANCE)
            .add(LineStringCodec.INSTANCE)
            .add(PointCodec.INSTANCE)
            .add(PolygonCodec.INSTANCE)
            .build();
}
