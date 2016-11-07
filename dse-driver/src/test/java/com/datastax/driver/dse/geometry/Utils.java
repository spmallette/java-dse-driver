/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
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
