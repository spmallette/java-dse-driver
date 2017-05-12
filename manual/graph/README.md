## Graph

This section exposes the APIs available through the _DataStax Java driver_ for interacting 
with a _DataStax Enterprise Graph_ Server.

Note that, as an alternative to those native APIs, the driver also provides an 
[integration with Apache Tinkerpop](../tinkerpop/).

This documentation is divided into the following sections:

- [Executing a query](#executing-a-query)
- [Handling results](#handling-results)
- [Graph options](#graph-options)
- [Query parameters](#query-parameters)

### Executing a query

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

_Note: you need to set `schema_mode: Development` in `dse.yaml` to run the example above._

A graph query can be executed either via the `executeGraph()` method that accepts a string query in parameter,
or the `executeGraph()` method that accepts a `GraphStatement`. A simple implementation of
a `GraphStatament` is the `SimpleGraphStatement`, which accepts a query string, and allows
to set specific graph options to this statement instance.

#### Asynchronous execution

Graph queries and statements can also be executed asynchronously via the `executeGraphAsync` methods.

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

The driver also exposes general purpose methods to handle results in the form of _Maps_ and _Lists_:

```java
GraphNode n = dseSession.executeGraph("g.V().valueMap()").one();
Map<String, Object> values = n.asMap();
```

#### Graph structural types

The driver has client-side representations for Vertex, Edge, Path, VertexProperty, and Property.

These are accessible via the corresponding `GraphNode#asXXXX()` methods:

```java
GraphNode n = dseSession.executeGraph("g.V().hasLabel('test_vertex')").one();
Vertex vertex = n.asVertex();

n = dseSession.executeGraph("g.V().hasLabel('test_vertex').outE()").one();
Edge edge = n.asEdge();

n = dseSession.executeGraph("g.V().hasLabel('test_vertex').outE().inV().path()").one();
Path path = n.asPath();

n = dseSession.executeGraph("g.V().hasLabel('test_vertex').next().property('propName')").one();
VertexProperty vertexProperty = n.asVertexProperty();
```

#### Data types compatibility matrix

_DSE Graph_ exposes several [data types](http://docs.datastax.com/en/latest-dse/datastax_enterprise/graph/reference/refDSEGraphDataTypes.html)
when defining a schema for a graph.

Those data types server-side translate into specific Java classes when the data is returned from the server.

Here is the exhaustive list of possible _DSE Graph_ data types, and their corresponding class
in the Java driver:

<table border="1" width="100%">
<tr><th>DSE Graph</th><th>Java Driver</th></tr>
<tr><td>bigint</td><td><tt>Long</tt></td></tr>
<tr><td>int</td><td><tt>Integer</tt></td></tr>
<tr><td>double</td><td><tt>Double</tt></td></tr>
<tr><td>float</td><td><tt>Float</tt></td></tr>
<tr><td>uuid</td><td><tt>UUID</tt></td></tr>
<tr><td>bigdecimal</td><td><tt>BigDecimal</tt></td></tr>
<tr><td>duration</td><td><tt>java.time.Duration</tt></td></tr>
<tr><td>inet</td><td><tt>InetAddress</tt></td></tr>
<tr><td>timestamp</td><td><tt>java.time.Instant</tt></td></tr>
<tr><td>time</td><td><tt>java.time.LocalTime</tt></td></tr>
<tr><td>date</td><td><tt>java.time.LocalDate</tt></td></tr>
<tr><td>smallint</td><td><tt>Short</tt></td></tr>
<tr><td>varint</td><td><tt>BigInteger</tt></td></tr>
<tr><td>polygon</td><td><tt>Polygon</tt></td></tr>
<tr><td>point</td><td><tt>Point</tt></td></tr>
<tr><td>linestring</td><td><tt>LineString</tt></td></tr>
<tr><td>blob</td><td><tt>byte[]</tt></td></tr>
</table>

#### Deserializing complex data types

The driver exposes methods to deserialize the data and return it into more complex data
types, as long as the server side data type associated corresponds. Doing so requires to use 
the `GraphNode#as(Class<T> clazz)` method:

```java
GraphNode n = dseSession.executeGraph("g.V().hasLabel('test_vertex')").one();
Vertex vertex = n.asVertex();
UUID uuidProp = vertex.getProperty("uuidProp").getValue().as(UUID.class);
```

#### Java 8 Time types

The _DSE Java driver_ is compatible with Java from version 6. The driver is able to 
automatically determine whether the application it's used with is running with a
Java runtime version less than version 6, and will be able to deserialize objects 
differently accordingly.

If using the driver with Java version 8 or higher, `java.time` types will be usable when 
retrieving the data from a Traversal (see the types matrix above). 
If the driver is used with an inferior version of Java, other classes will be usable when 
retrieving the data sent by the server.

For Java versions < 8, the `Duration()` and `Time()` DSE Graph types will be exposed as Strings.
`Timestamp()` will be exposed as a `java.util.Date`, and `Date()` as a `com.datastax.driver.core.LocalDate`.

#### A word on Properties

The vertex properties exposed by the driver (_VertexProperty_) respect the same behaviour as 
in Apache TinkerPop. First, A _VertexProperty_ is a property, with a simple value. But in 
addition to that, a _VertexProperty_ can be the parent of a sublist of _Property_. 
If that's the case, the _VertexProperty_ is a _meta property_.

Moreover, for a _Vertex_ the *same* property key can be associated to multiple properties. 
This is called a _multi value property_.

Altogether, a _Vertex_ can potentially have _multi value meta properties_.

Here is the syntax for dealing with properties with the _DataStax Java driver_ graph types:

```java
GraphNode n = dseSession.executeGraph("g.V().hasLabel('test_vertex_meta_props')").one();
Vertex vertex = n.asVertex();

// there can be more than one VertexProperty with the key "meta_property"
Iterator<VertexProperty> metaProps = vertex.getProperties("meta_property");

VertexProperty metaProp1 = metaProps.next();
// the value of the meta property
int metaProp1Value = metaProp1.getValue().asInt();
// the properties of the meta property itself
Iterator<Property> simpleProps1 = metaProp1.getProperties();
Property simpleProp11 = simpleProps1.next();
double simplePropValue11 = simpleProp11.getValue().asDouble(); 
Property simpleProp12 = simpleProps1.next();
double simplePropValue12 = simpleProp12.getValue().asDouble(); 

// **multi value** meta property.
VertexProperty metaProp2 = metaProps.next();
[...]
```

More on how to create and query multi value meta properties in the
[Apache Tinkerpop documentation](http://tinkerpop.apache.org/docs/3.2.3/reference/#vertex-properties).

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

The higher time limit for executing a Graph query is defined server side, in `dse.yaml`.

By default the Java driver will rely on that option that is declared server-side. This means that by default, after sending a request, the driver will wait until the server responds with a result or an error message, or times out.

This can be changed if the client needs a lower timeout. A timeout for the client can be 
set either on the Cluster’s GraphOptions object and will apply to all Graph queries, or 
individually on each GraphStatement object, through the methods setReadTimeoutMillis(). Note 
that the server will abort a query once the client has stopped waiting for it, so there’s 
no risk of leaving long-running queries on the server.

### Query Parameters

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

Another way to specify parameters is to use the `set()` call on a `SimpleGraphStatement`:

```java
SimpleGraphStatement s = new SimpleGraphStatement("g.addV(label, vertexLabel)")
        .set("vertexLabel", "test_vertex_2");
dseSession.executeGraph(s);
```

Note that, unlike in CQL, Gremlin placeholders are not prefixed with ":".

The classes supported for the parameters are listed in the [Data types compatibility matrix](#data-types-compatibility-matrix).

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

### Prepared statements

Prepared graph statements are not supported by DSE yet (they will be added in the near future).