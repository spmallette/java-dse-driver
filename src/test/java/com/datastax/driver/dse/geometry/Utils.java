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

import com.datastax.driver.core.utils.Bytes;

import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;

public class Utils {
    /**
     * Can be used with static imports as a shortcut for {@code new Point(x, y)}.
     */
    public static Point p(double x, double y) {
        return new Point(x, y);
    }

    static Object serializeAndDeserialize(Geometry geometry) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);

        out.writeObject(geometry);

        byte[] bytes = baos.toByteArray();
        byte[] wkb = Bytes.getArray(geometry.asWellKnownBinary());
        assertThat(bytes).containsSequence(wkb);

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
        return in.readObject();
    }
}
