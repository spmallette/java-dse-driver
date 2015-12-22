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

import com.datastax.driver.geometry.Geometry;
import org.testng.annotations.Test;

import java.io.*;

import static com.datastax.driver.core.ProtocolVersion.V4;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"TestNGDataProvider", "unused"})
public abstract class GeometryCodecTest<G extends Geometry, C extends GeometryCodec<G>> {

    private C codec;

    protected GeometryCodecTest(C codec) {
        this.codec = codec;
    }

    public abstract Object[][] serde();

    public abstract Object[][] format();

    public abstract Object[][] parse();

    @Test(groups = "unit", dataProvider = "serde")
    public void should_serialize_and_deserialize(G input, G expected) throws Exception {
        assertThat(codec.deserialize(codec.serialize(input, V4), V4)).isEqualTo(expected);
        assertThat(deserialize(serialize(input))).isEqualTo(expected);
    }

    @Test(groups = "unit", dataProvider = "format")
    public void should_format(G input, String expected) {
        assertThat(codec.format(input)).isEqualTo(expected);
    }

    @Test(groups = "unit", dataProvider = "parse")
    public void should_parse(String input, G expected) {
        assertThat(codec.parse(input)).isEqualTo(expected);
    }

    private static byte[] serialize(Object o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(baos);
        os.writeObject(o);
        os.flush();
        os.close();
        return baos.toByteArray();
    }

    private static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object o = ois.readObject();
        bais.close();
        return o;
    }

}
