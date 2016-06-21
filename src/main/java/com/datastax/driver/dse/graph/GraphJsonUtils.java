/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;


import com.datastax.driver.core.VersionNumber;
import com.datastax.driver.dse.DseCluster;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
class GraphJsonUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphJsonUtils.class);

    static final boolean JSR_310_AVAILABLE;

    static {
        boolean jsr310Available;
        try {
            Class.forName("java.time.Instant");
            jsr310Available = true;
        } catch (LinkageError e) {
            jsr310Available = false;
            LOGGER.warn("JSR 310 could not be loaded", e);
        } catch (ClassNotFoundException e) {
            jsr310Available = false;
        }
        JSR_310_AVAILABLE = jsr310Available;
    }

    private static final boolean TINKERPOP_AVAILABLE;

    static {
        boolean tinkerpopAvailable;
        try {
            Class.forName("org.apache.tinkerpop.gremlin.structure.Edge");
            tinkerpopAvailable = true;
        } catch (LinkageError e) {
            tinkerpopAvailable = false;
            LOGGER.warn("Tinkerpop API could not be loaded", e);
        } catch (ClassNotFoundException e) {
            tinkerpopAvailable = false;
        }
        TINKERPOP_AVAILABLE = tinkerpopAvailable;
    }

    static final GraphJsonUtils INSTANCE = new GraphJsonUtils();

    private final ObjectMapper objectMapper;

    private GraphJsonUtils() {
        objectMapper = new ObjectMapper();
        Version dseDriverVersion = dseDriverVersion();
        objectMapper.registerModule(new DefaultGraphModule("graph-default", dseDriverVersion));
        if (JSR_310_AVAILABLE) {
            LOGGER.debug("JSR 310 found on the classpath, registering serializers for java.time temporal types");
            objectMapper.registerModule(new Jdk8Jsr310Module("graph-jsr310", dseDriverVersion));
        } else {
            LOGGER.debug("JSR 310 not found on the classpath, not registering serializers for java.time temporal types");
        }
        if (TINKERPOP_AVAILABLE) {
            LOGGER.debug("Tinkerpop API found on the classpath, registering Tinkerpop serializers");
            objectMapper.registerModule(new TinkerGraphModule("graph-tinkerpop", dseDriverVersion));
        } else {
            LOGGER.debug("Tinkerpop API not found on the classpath, not registering Tinkerpop serializers");
        }
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
     * <p>
     * If Tinkerpop API is available, nodes in the returned tree can be coerced into one of the following
     * Tinkerpop API interfaces:
     * {@link org.apache.tinkerpop.gremlin.structure.Vertex Vertex},
     * {@link org.apache.tinkerpop.gremlin.structure.Edge Edge} and
     * {@link org.apache.tinkerpop.gremlin.process.traversal.Path Path}.
     * <p>
     * Example:
     * <pre>{@code
     * GraphNode result = session.executeGraph("g.V().has('name', 'robert').next()").one();
     * Vertex robert = result.as(org.apache.tinkerpop.gremlin.structure.Vertex.class);
     * VertexProperty<String> nickname = robert.property("nickname");
     * String bob = nickname.value();
     * }</pre>
     */
    GraphNode readStringAsTree(String content) {
        try {
            return new DefaultGraphNode(objectMapper.readTree(content), objectMapper);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * {@inheritDoc}
     * If JDK 8 is available, then the following temporal types can be serialized:
     * ({@code java.time.Instant}, {@code java.time.ZonedDateTime}, and {@code java.time.Duration}).
     * <p>
     * If Tinkerpop API is available, then
     * the following Tinkerpop API interfaces can be serialized:
     * {@link org.apache.tinkerpop.gremlin.structure.Vertex Vertex},
     * {@link org.apache.tinkerpop.gremlin.structure.Edge Edge} and
     * {@link org.apache.tinkerpop.gremlin.structure.VertexProperty VertexProperty}.
     * <p>
     * These elements are serialized to their identifiers.
     * <p>
     * Note that instances of Tinkerpop's
     * {@link org.apache.tinkerpop.gremlin.structure.Property Property} and
     * {@link org.apache.tinkerpop.gremlin.process.traversal.Path Path}
     * cannot be serialized.
     */
    String writeValueAsString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
