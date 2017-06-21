/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;


import com.datastax.driver.core.Row;
import com.datastax.driver.core.VersionNumber;
import com.datastax.driver.core.exceptions.DriverException;
import com.datastax.driver.dse.DseCluster;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.datastax.driver.core.ClasspathUtil.isJavaTimeAvailable;

/**
 * Utility static methods and objects useful in the DSE Driver's execution chain.
 */
public class GraphJsonUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphJsonUtils.class);

    private static final ObjectMapper GRAPHSON1_OBJECT_MAPPER;
    private static final ObjectMapper GRAPHSON2_OBJECT_MAPPER;

    /**
     * Function that transforms a Row from the wrapped Cassandra driver, into a {@link com.datastax.driver.dse.graph.GraphNode}.
     * The GraphNode created will be directly exposed to the user when iterating over the
     * {@link com.datastax.driver.dse.graph.GraphResultSet} returned from a Graph query.
     */
    public static final Function<Row, GraphNode> ROW_TO_GRAPHSON2_OBJECTGRAPHNODE = new Function<Row, GraphNode>() {
        @Override
        public GraphNode apply(Row input) {
            try {
                if (input == null) {
                    return null;
                } else if (input.getColumnDefinitions().contains("gremlin")) {
                    // some results do not contain the "row" gremlin.
                    return readStringAsTreeGraphson20(input.getString("gremlin")).get("result");
                } else {
                    return null;
                }
            } catch (Exception e) {
                throw new DriverException("Could not deserialize the response of this Graph query.", e);
            }
        }
    };

    static {
        GRAPHSON1_OBJECT_MAPPER = new ObjectMapper();
        Version dseDriverVersion = dseDriverVersion();
        GRAPHSON1_OBJECT_MAPPER.registerModule(new GraphSON1DefaultModule("graph-graphson1default", dseDriverVersion));

        GraphSON2Mapper.Builder graphSON2MapperBuilder = GraphSON2Mapper.build()
                .addCustomModule(new GraphSON2GremlinDriverModule())
                .addCustomModule(new GraphSON2GremlinXDriverModule())
                .addCustomModule(new GraphSON2GremlinGraphDriverModule())
                .addCustomModule(new GraphSON2DseGraphDriverModule())
                .addCustomModule(new GraphSON2TinkerDriverModule())
                .addCustomModule(new GraphSON2DriverObjectsModule());

        if (isJavaTimeAvailable()) {
            LOGGER.debug("JSR 310 found on the classpath, registering serializers for java.time temporal types");
            GRAPHSON1_OBJECT_MAPPER.registerModule(new GraphSON1JavaTimeModule("graph-graphson1javatime", dseDriverVersion));
            GRAPHSON1_OBJECT_MAPPER.registerModule(new GraphSON1DriverTimeModule("graph-graphson1drivertime", dseDriverVersion));
            graphSON2MapperBuilder.addCustomModule(new GraphSON2JavaTimeModule());
        } else {
            LOGGER.debug("JSR 310 not found on the classpath, not registering serializers for java.time temporal types");
            GRAPHSON1_OBJECT_MAPPER.registerModule(new GraphSON1DriverTimeModule("graph-graphson1drivertime", dseDriverVersion));
            graphSON2MapperBuilder.addCustomModule(new GraphSON2DriverTimeModule());
        }
        GRAPHSON2_OBJECT_MAPPER = graphSON2MapperBuilder.create().createMapper();
    }

    private static Version dseDriverVersion() {
        String versionStr = DseCluster.getDseDriverVersion();
        VersionNumber version = VersionNumber.parse(versionStr);
        return new Version(
                version.getMajor(), version.getMinor(), version.getPatch(),
                // version.getPreReleaseLabels() throws NPE, see JAVA-1160
                versionStr.contains("-SNAPSHOT") ? "SNAPSHOT" : null,
                "com.datastax.cassandra", "dse-driver");
    }

    /**
     * If JDK 8 is available, then node properties in the returned tree can be coerced into
     * JDK 8 temporal types ({@code java.time.Instant}, {@code java.time.ZonedDateTime}, and {@code java.time.Duration}).
     * <p>
     * Example:
     * <pre>{@code
     * GraphNode result = session.executeGraph("g.V().has('name', 'robert').next()").one();
     * Vertex robert = result.asVertex();
     * VertexProperty birthday = robert.getProperty("birthday");
     * Instant t = birthday.getValue().as(Instant.class);
     * }</pre>
     */
    static GraphNode readStringAsTree(String content) {
        try {
            return new DefaultGraphNode(GRAPHSON1_OBJECT_MAPPER.readTree(content), GRAPHSON1_OBJECT_MAPPER);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * If JDK 8 is available, then the following temporal types can be serialized:
     * ({@code java.time.Instant}, {@code java.time.ZonedDateTime}, and {@code java.time.Duration}).
     */
    static String writeValueAsString(Object value) {
        try {
            return GRAPHSON1_OBJECT_MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    static GraphNode readStringAsTreeGraphson20(String content) {
        try {
            return new ObjectGraphNode(GRAPHSON2_OBJECT_MAPPER.readValue(content, Object.class));
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * If JDK 8 is available, then the following temporal types can be serialized:
     * ({@code java.time.Instant}, {@code java.time.ZonedDateTime}, and {@code java.time.Duration}).
     */
    static String writeValueAsStringGraphson20(Object value) {
        try {
            return GRAPHSON2_OBJECT_MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
