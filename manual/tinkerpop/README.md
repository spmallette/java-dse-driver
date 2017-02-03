## Apache TinkerPop client integration

As an alternative to its [native graph API](../graph/), the DataStax Enterprise Java driver provides an integration 
layer to interact with DSE through the [Apache TinkerPop][tinkerpop] library.

This component is published in Maven central as a separate artifact:

```xml
<dependency>
  <groupId>com.datastax.cassandra</groupId>
  <artifactId>java-dse-graph</artifactId>
  <version>1.2.0-eap4</version>
</dependency>
```

[tinkerpop]: http://tinkerpop.apache.org/

### DataStax drivers execution compatibility

This new package provides the necessary tools to get the most out the popular _Apache TinkerPop_
Traversal API, while still getting the benefits of the DataStax drivers execution model.
A Traversal is considered as a query, that can then be wrapped inside a _GraphStatement_.

Here's how to create a _DSE Java driver_'s _GraphStatement_ out of a _Apache TinkerPop_ _Traversal_:

```java
// traversal() returns a simple GraphTraversalSource that is not meant to be iterated itself
GraphTraversalSource g = DseGraph.traversal();

GraphStatement graphStatement = DseGraph.statementFromTraversal(g.V().has("name", "marko"));
GraphResultSet grs = dseSession.executeGraph(graphStatement);

// the API does not change, whether what's executed is a String, or a Statement created from a Traversal, and so on.
for (GraphNode graphNode : grs) {
    Vertex v = graphNode.asVertex();
}
```

Statements created from Traversal instances will behave, with regards to the cluster,
the same way the _DSE Java driver_ behaves with DSE. All the advanced features of the _DSE Java driver_ come into play, 
automatic retries, load balancing, DataCenter awareness, smart query logging, and so on.

Information about the returned result types with the _DSE Java driver_ can be found on 
[this page](http://docs.datastax.com/en/developer/java-driver-dse/1.1/manual/graph/#handling-results).

### TinkerPop direct compatibility
This package also provides full compatibility with _Apache TinkerPop_'s query execution model
 and result types.

Here's an example of how to get a _Apache TinkerPop_ _GraphTraversalSource_ that is remotely connected
to a _DseGraph_ server, communicating internally via the _DSE Java driver_:

```java
DseCluster dseCluster = DseCluster.builder()
    .addContactPoint("1.2.3.4")
    .withGraphOptions(new GraphOptions()
    .setGraphName("mygraph"))
    .build();
DseSession dseSession = dseCluster.connect();

GraphTraversalSource g = DseGraph.traversal(dseSession);

// Now you can use the Traversal source and use it **as if** it was working against a local graph, and with the usual TinkerPop API. All the communication with the DSE Graph server is done transparently.
List<Vertex> vertices = g.V().hasLabel("person").toList();
```

Traversal sources with different configurations can easily be created. By default the options
specific to _DSE Graph_ are taken from the _DseCluster_ configuration, however
the API exposes a way to override each individual setting, per traversal source:

```java
GraphTraversalSource gOLTP = DseGraph.traversal(dseSession, new GraphOptions().setGraphName("mygraph"));
GraphTraversalSource gOLAP = DseGraph.traversal(dseSession, new GraphOptions().setGraphName("myothergraph").setGraphSource(ANALYTICS_SOURCE_NAME));

Vertex v = gOLTP.V().has("name", "marko").next();
long count = gOLAP.V().count().next();
```

#### A word on Results

Objects returned after the Remote traversal execution are objects that are detached from
the original DSE Graph on the server. A _detached_ element is an element that lives on
its own, even though the _detached_ elements contains the complete data, modifications
made to a _detached_ element **do not affect** the data stored in the _DSE Graph_.


### Search and Geo

For ease of use, _DSE Search_ and _Geo_ predicates are directly integrated and provided
in the programmatic API:

```java
GraphTraversalSource g = DseGraph.traversal(dseSession, new GraphOptions().setGraphName("thegraph"));
Vertex v = g.V().has("textProp", Search.tokenPrefix("1")).next();
```

Or:

```java
GraphTraversalSource g = DseGraph.traversal(dseSession, new GraphOptions().setGraphName("thegraph"));

Vertex v1 = g.addV(T.label, "geopoint", "point", Geo.point(50, 50)).next();
Vertex v2 = g.addV(T.label, "geopoint", "point", Geo.point(120, 120)).next();

List<Vertex> v3 = g.V().has("point", Geo.inside(49, 49, 4)).toList();
        
assert v3.size() == 1;
assert v1 == v3.get(0);
```

Please check out the Javadoc of the _Geo_ and _Search_ classes for more information. 

### Programmatic Schema API

Available soon!...