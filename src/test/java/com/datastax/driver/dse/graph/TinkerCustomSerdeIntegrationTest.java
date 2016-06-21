/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.datastax.driver.core.utils.DseVersion;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

@DseVersion(major = 5.0)
public class TinkerCustomSerdeIntegrationTest extends CustomSerdeIntegrationTest {

    @Test(groups = "short")
    public void should_deserialize_vertex_as_tinker_type() throws Exception {
        GraphNode result = session().executeGraph("g.V().has('name', 'neptune').next()").one();
        org.apache.tinkerpop.gremlin.structure.Vertex neptune = result.as(org.apache.tinkerpop.gremlin.structure.Vertex.class);
        Assertions.assertThat(neptune).isNotNull();
        Assertions.assertThat(neptune.property("name").value()).isEqualTo("neptune");
        Assertions.assertThat(neptune.property("age").value()).isEqualTo(4500);
        Iterator<VertexProperty<String>> nicknames = neptune.properties("nicknames");
        assertThat(nicknames)
                .extracting(TinkerGraphExtractors.<String>vertexPropertyValue())
                .containsExactly("Neppy", "Flipper");
        // use the retrieved vertex to query again
        org.apache.tinkerpop.gremlin.structure.Vertex neptune2 = session().executeGraph(new SimpleGraphStatement("g.V(v).next()").set("v", neptune)).one()
                .as(org.apache.tinkerpop.gremlin.structure.Vertex.class);
        Assertions.assertThat(neptune2).isNotNull();
        Assertions.assertThat(neptune2).isEqualTo(neptune);
    }

}
