## Changelog

### 1.2.0-eap2 (in progress)

- [improvement] JAVA-1335: Show Row-Level Access Control in DSE CQL metadata.
- [bug] JAVA-1330: Add un/register for SchemaChangeListener in DelegatingCluster
- [bug] JAVA-1351: Include Custom Payload in Request.copy.
- [improvement] JAVA-1264: Provide support for ProxyAuthentication.


### 1.1.1-beta1

- [improvement] JAVA-1250: Provide a Fluent API for DSE Graph.


### 1.1.0

- [improvement] JAVA-1251: Update to depend on cassandra-driver-core 3.1.0.


### 1.0.0

- [improvement] JAVA-1222: Add GraphStatement.setIdempotent.
- [improvement] JAVA-1225: Log warnings about the new retry policy behavior.
- [bug] JAVA-1230: Force Well-Known Binary encoding to little-endian.


### 1.0.0-eap5

- [bug] JAVA-1183: Change default traversal source to 'g'.
- [improvement] JAVA-1081: Vertex Properties don't handle multiple cardinality / rich properties.
- [improvement] JAVA-1146: Graph module serde improvements.
- [improvement] JAVA-1125: Improvements to Path class.
- [improvement] JAVA-1066: Enable per-GraphStatement and GraphOptions socket timeouts.


### 1.0.0-eap4

- [new feature] JAVA-1098: Route graph analytics queries to the Spark master.
- [bug] JAVA-1076: Handle WKT 'EMPTY' keyword in Geospatial types.
- [improvement] JAVA-1106: Remove Circle type.
- [improvement] JAVA-1129: Don't expose graph alias in user API.
- [improvement] JAVA-1104: Expose methods to set CLs and Timestamp on GraphStatement.


### 1.0.0-eap3

- [new feature] JAVA-1107: add Geometry#contains().


### 1.0.0-eap2

- [new feature] JAVA-1080: Expose automatic inference of GraphResult.


### 1.0.0-eap1

- [new feature] JAVA-1022: Geospatial types.
- [new feature] JAVA-864: Initial graph integration.
- [improvement] JAVA-1024: Allow overriding of default JAAS login configuration.
