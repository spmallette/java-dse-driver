# Java driver for DataStax Enterprise

This driver is built on top of the [CQL driver][core] 3.0.0, with specific extensions for DSE:

* `Authenticator` implementations that use the authentication scheme negotiation in the server-side `DseAuthenticator`;
* value classes for geospatial types, and type codecs that integrate them seamlessly with the driver;
* DSE graph integration.

[core]: http://datastax.github.io/java-driver/



## Installing

The driver is distributed as a binary tarball with the following structure:

* `README.md`: this file;
* `dse-driver-<version>.jar`: main DSE driver artifact;
* `lib/*.jar`: runtime dependencies;
* `apidocs/*`: Javadoc API reference;
* `src/*.zip`: source files.

### Including the binaries in your project

Copy all the binaries (main DSE driver artifact and runtime dependencies) into your project, and make sure they are
included in your runtime classpath.

Some of the dependencies are optional and may be excluded:

* `snappy-java-1.0.5.jar` and `lz4-1.2.0.jar` are only necessary if you enable client-to-server [compression] with the
  corresponding algorithm;
* `HdrHistogram-2.1.4.jar` is only necessary if you enable percentile-based [query logging][querylogger] or [speculative
  executions][specex].

### Using a Maven repository

The DSE driver is not available from a public Maven repository, but all of its dependencies are. If your organization
uses an internal Maven repository, you can simply deploy the main artifact there:

```
unzip -p dse-driver-<version>.jar \
    META-INF/maven/com.datastax.cassandra/dse-driver/pom.xml > pom.xml

mvn org.apache.maven.plugins:maven-deploy-plugin:2.8.2:deploy-file \
    -Dfile=dse-driver-<version>.jar \
    -DpomFile=pom.xml \
    -Durl=http:/some/url \
    -DrepositoryId=some.id
```

And then reference the driver from your application with the following coordinates:

```xml
<dependency>
    <groupId>com.datastax.cassandra</groupId>
    <artifactId>dse-driver</artifactId>
    <version>...</version>
</dependency>
```

By default, this will not include the driver's optional dependencies. If you use a feature that requires one (see the
previous section), you'll have to redeclare the dependency explicitly in your application.

[compression]: http://datastax.github.io/java-driver/manual/compression/
[QueryLogger]: http://datastax.github.io/java-driver/manual/logging/#logging-query-latencies
[specex]: http://datastax.github.io/java-driver/manual/speculative_execution/



## Getting started

`DseCluster` and `DseSession` wrap their CQL driver counterparts. All CQL features are available (see the
[CQL driver manual][core-manual]), so you can use a `DseSession` in lieu of a `Session`:

```java
import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.DseSession;

DseCluster dseCluster = null;
try {
    dseCluster = DseCluster.builder()
            .addContactPoint("127.0.0.1")
            .build();
    DseSession dseSession = dseCluster.connect();

    Row row = dseSession.execute("select release_version from system.local").one();
    System.out.println(row.getString("release_version"));
} finally {
    if (dseCluster != null) dseCluster.close();
}
```

[core-manual]: http://datastax.github.io/java-driver/manual/



## Authentication

For clients connecting to a DSE cluster secured with `DseAuthenticator`, two authentication providers are included:

* `DsePlainTextAuthProvider`: plain-text authentication;
* `DseGSSAPIAuthProvider`: GSSAPI authentication.

To configure a provider, pass it when initializing the cluster:

```java
import com.datastax.driver.dse.auth.DseGSSAPIAuthProvider;

DseCluster dseCluster = DseCluster.builder()
        .addContactPoint("127.0.0.1")
        .withAuthProvider(new DseGSSAPIAuthProvider())
        .build();
```

See the Javadocs of each implementation for more details.



## Geospatial types

DSE 5 comes with a set of additional types to represent geospatial data: `PointType`, `LineStringType`, and
`PolygonType`:

```
cqlsh> CREATE TABLE points_of_interest(name text PRIMARY KEY, coords 'PointType');
cqlsh> INSERT INTO points_of_interest (name, coords) VALUES ('Eiffel Tower', 'POINT(48.8582 2.2945)');
```

The DSE driver includes Java representations of these types, that can be used directly in queries:

```java
import com.datastax.driver.dse.geometry.Point;

Row row = dseSession.execute("SELECT coords FROM points_of_interest WHERE name = 'Eiffel Tower'").one();
Point coords = row.get("coords", Point.class);

dseSession.execute("INSERT INTO points_of_interest (name, coords) VALUES (?, ?)",
        "Washington Monument", new Point(38.8895, 77.0352));
```

This integration is made possible by [custom type codecs][core-codecs]. The DSE driver automatically registers a set of
geospatial codecs at startup, in the `CodecRegistry` that was specified for your cluster.

If you're not going to use geospatial types, you can prevent the codecs from being registered by calling
`withoutGeospatialCodecs()` on the cluster builder (although leaving them does not have any significant impact).

[core-codecs]: http://datastax.github.io/java-driver/manual/custom_codecs/



## Graph

`DseSession` has dedicated methods to execute graph queries:

```java
import com.datastax.driver.dse.graph.GraphStatement;
import com.datastax.driver.dse.graph.SimpleGraphStatement;

dseSession.executeGraph("system.graph('demo').ifNotExists().create()");

GraphStatement s1 = new SimpleGraphStatement("g.addV(label, 'test_vertex')").setGraphName("demo");
dseSession.executeGraph(s1);

GraphStatement s2 = new SimpleGraphStatement("g.V()").setGraphName("demo");
GraphResultSet rs = dseSession.executeGraph(s2);
System.out.println(rs.one().asVertex());
```

### Graph options

You can set default graph options when initializing the cluster. They will be used for all graph statements. For
example, to avoid repeating `setGraphName("demo")` on each statement:

```java
DseCluster dseCluster = DseCluster.builder()
        .addContactPoint("127.0.0.1")
        .withGraphOptions(new GraphOptions().setGraphName("demo"))
        .build();
```

You can also retrieve and change the options at runtime (be careful about concurrency though, the changes will be
visible across all client threads):

```java
GraphOptions graphOptions = dseCluster.getConfiguration().getGraphOptions();
graphOptions.setGraphName("demo2");
```

If an option is set manually on a `GraphStatement`, it always takes precedence; otherwise the default option is used.
This might be a problem if a default graph name is set, but you explicitly want to execute a statement targeting
`system`, for which no graph name must be set. In that situation, use `GraphStatement#setSystemQuery()`:

```java
GraphStatement s = new SimpleGraphStatement("system.graph('demo').ifNotExists().create()")
        .setSystemQuery();
dseSession.executeGraph(s);
```

#### Timeouts 

The higher time limit for executing a Graph query is defined server side, in the `dse.yaml`.

By default the Java driver will rely on that option that is declared server-side. This means that by default,
after sending a request, the driver will wait until the server responds with a result or an error message, or times out.

This can be changed if the client needs a lower timeout. A timeout for the client can be set either on the Cluster's 
`GraphOptions` object and will apply to all Graph queries, or individually on each `GraphStatement` object, through
the methods `setReadTimeoutMillis()`. Note that the server will abort a query once the client has stopped waiting for
it, so there's no risk of leaving long-running queries on the server.

### Query execution

As seen already, graph statements can be executed with the session's `executeGraph` method. There is also an
asynchronous equivalent called `executeGraphAsync`.

If you don't need any specific configuration on the statement, `DseSession` provides a convenient shortcut that accepts
the query string directly:

```java
GraphResultSet rs = dseSession.executeGraph("g.V()");

// Is the same as:
GraphResultSet rs2 = dseSession.executeGraph(new SimpleGraphStatement("g.V()"));
```

### Handling results

Graph queries return a `GraphResultSet`, which is essentially an iterable of `GraphNode`:

```java
GraphResultSet rs = dseSession.executeGraph("g.V()");

// Iterating:
for (GraphNode n : rs) {
    System.out.println(n);
}

// Get the first result only (or if you know there is exactly one):
GraphNode n = rs.one();
```

`GraphNode` wraps the responses returned by the server. You can coerce the result to a specific type using the
`asXxx` methods:

```java
GraphNode n = dseSession.executeGraph("g.V().count()").one();
System.out.printf("The graph has %s vertices%n", n.asInt());
```

If the result is an array or an object (non-leaf node), you can iterate its child elements:

```java
if (n.isArray()) {
    for (int i = 0; i < n.size(); i++) {
        GraphNode child = n.get(i);
        System.out.printf("Element at position %d: %s%n", i, child);
    }
}

if (n.isObject()) {
    Iterator<String> fieldNames = n.fieldNames();
    while (fieldNames.hasNext()) {
        String fieldName = fieldNames.next();
        System.out.printf("Element at key %s: %s%n", fieldName, n.get(fieldName));
    }
}
```

The driver also has client-side representations for vertices, edges and paths:

```java
GraphNode n = dseSession.executeGraph("g.V().hasLabel('test_vertex')").one();
Vertex vertex = n.asVertex();

n = dseSession.executeGraph("g.V().hasLabel('test_vertex').outE()").one();
Edge edge = n.asEdge();

n = dseSession.executeGraph("g.V().hasLabel('test_vertex').outE().inV().path()").one();
Path path = n.asPath();
```

### Parameters

Graph query parameters are always named. Parameter bindings are passed as a `Map<String, Object>` alongside the query
(Guava's `ImmutableMap` provides a convenient way to build maps on the fly):

```java
import com.google.common.collect.ImmutableMap;

// One-liner:
dseSession.executeGraph("g.addV(label, vertexLabel)",
        ImmutableMap.<String, Object>of("vertexLabel", "test_vertex_2"));

// Alternative syntax:
dseSession.executeGraph("g.addV(label, vertexLabel)",
        ImmutableMap.<String, Object>builder()
                .put("vertexLabel", "test_vertex_2")
                .build());
```

Another way to specify parameters is to chain `set()` calls on a statement:

```java
SimpleGraphStatement s = new SimpleGraphStatement("g.addV(label, vertexLabel)")
        .set("vertexLabel", "test_vertex_2");
dseSession.executeGraph(s);
```

Note that, unlike in CQL, Gremlin placeholders are not prefixed with ":".

Parameters can have the following types:

* `null`;
* boolean, numeric or `String`;
* Java arrays or `List` instances;
* Java maps.

In addition, you can inject:

* any `GraphNode` instance. In particular, the identifier of a previously retrieved vertex or edge:

    ```java
    Vertex v1 = dseSession.executeGraph("g.V().hasLabel('test_vertex')").one().asVertex();
    Vertex v2 = dseSession.executeGraph("g.V().hasLabel('test_vertex_2')").one().asVertex();

    SimpleGraphStatement s = new SimpleGraphStatement(
            "def v1 = g.V(id1).next()\n" +
                    "def v2 = g.V(id2).next()\n" +
                    "v1.addEdge('relates', v2)")
            .set("id1", v1.getId())
            .set("id2", v2.getId());

    dseSession.executeGraph(s);
    ```

* any instance of `Element`. Its identifier will be serialized, so the following is equivalent to the last example above:

    ```java
    Vertex v1 = dseSession.executeGraph("g.V().hasLabel('test_vertex')").one().asVertex();
    Vertex v2 = dseSession.executeGraph("g.V().hasLabel('test_vertex_2')").one().asVertex();

    SimpleGraphStatement s = new SimpleGraphStatement(
            "def v1 = g.V(id1).next()\n" +
                    "def v2 = g.V(id2).next()\n" +
                    "v1.addEdge('relates', v2)")
            .set("id1", v1)
            .set("id2", v2);

    dseSession.executeGraph(s);
    ```

* a geospatial type:

    ```java
    SimpleGraphStatement s = new SimpleGraphStatement(
            "g.V().hasLabel('test_vertex').property('location', coords)"
    ).set("coords", new Point(38.8895, 77.0352));
    dseSession.executeGraph(s);
    ```

### Prepared statements

Prepared graph statements are not supported by DSE yet (they will be added in the near future).

### Using Tinkerpop Gremlin Core API

It is possible to configure the driver to support Tinkerpop Gremlin Core API.

When this feature is enabled, results can be coerced to Tinkerpop API classes. The driver will also
accept parameters that implement that API.

There are some pre-requisites before using Tinkerpop Gremlin Core API:

* Your application must be compiled with JDK 8+.
* You need to explicitly include Tinkerpop Gremlin Core API in your classpath.

    If your are using Maven, this can be achieved with the following dependency:


    ```xml
    <dependency>
        <groupId>org.apache.tinkerpop</groupId>
        <artifactId>gremlin-driver</artifactId>
        <version>3.2.0-incubating</version>
    </dependency>
    ```

__WARNING: please make sure that your version of Tinkerpop is compatible.
The driver has been compiled and tested against version 3.2.0-incubating;
it does NOT provide any compatibility guarantees for older Tinkerpop versions.__
 
If the above prerequisites are met, support for Tinkerpop Gremlin Core API
will be transparently activated by the driver.
 
Here is an example of how to coerce query results to Tinkerpop interfaces:

    ```java
    import org.apache.tinkerpop.gremlin.structure.*;
    
    DseSession dseSession = dseCluster.connect();
    Vertex john = dseSession.executeGraph("g.V().has('name', 'john').next()").one().as(Vertex.class);
    VertexProperty<Integer> ageProperty = john.properties("age");
    int age = ageProperty.value();
    ```

When deserializing, the driver supports conversions to the following Tinkerpop interfaces:
    
* `org.apache.tinkerpop.gremlin.structure.Vertex`
* `org.apache.tinkerpop.gremlin.structure.Edge`
* `org.apache.tinkerpop.gremlin.structure.Property`
* `org.apache.tinkerpop.gremlin.structure.VertexProperty`
* `org.apache.tinkerpop.gremlin.process.traversal.Path`

When serializing, the driver supports conversions from the following Tinkerpop interfaces:

* `org.apache.tinkerpop.gremlin.structure.Vertex`
* `org.apache.tinkerpop.gremlin.structure.Edge`
* `org.apache.tinkerpop.gremlin.structure.VertexProperty`

