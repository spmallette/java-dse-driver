## Changelog

### 1.2.0-rc1

- [bug] JAVA-1406: Handle unprepared error in continuous paging.
- [improvement] JAVA-1409: Upgrade to tinkerpop 3.2.4.

Merged from OSS 3.x:

- [new feature] JAVA-1364: Enable creation of SSLHandler with remote address information.
- [improvement] JAVA-1308: CodecRegistry performance improvements.
- [improvement] JAVA-1241: Upgrade Netty to 4.1.x.
- [improvement] JAVA-1287: Add CDC to TableOptionsMetadata and Schema Builder.
- [improvement] JAVA-1392: Reduce lock contention in RPTokenFactory.
- [improvement] JAVA-1328: Provide compatibility with Guava 20.
- [improvement] JAVA-1247: Disable idempotence warnings.
- [improvement] JAVA-1286: Support setting and retrieving udt fields in QueryBuilder.
- [bug] JAVA-1404: Fix min token handling in TokenRange.contains.


### 1.2.0-eap5

- [bug] JAVA-1390: Preserve original error when a continuous paging query times out on the client.
- [improvement] JAVA-1391: Fail if batch children use proxy auth.
- [improvement] JAVA-1319: Add support for DSE DateRangeType.

Merged from OSS 3.1.x:

- [bug] JAVA-1313: Copy SerialConsistencyLevel to PreparedStatement.
- [documentation] JAVA-1334: Clarify documentation of method `addContactPoints`.
- [improvement] JAVA-1357: Document that getReplicas only returns replicas of the last token in range.

Cherry-picked from OSS 3.x:

- [new feature] JAVA-1362: Send query options flags as [int] for Protocol V5+.
- [bug] JAVA-1397: Handle duration as native datatype in protocol v5+.


### 1.2.0-eap4

- [bug] JAVA-1374: Reintroduce Geo.inside(V).
- [improvement] JAVA-1372: Expose routing token instead of range.
- [new feature] JAVA-1381: Add Units to Geo.inside() predicates.
- [improvement] JAVA-1375: Improve error management for continuous queries
- [bug] JAVA-1383: Add java-dse-graph and dependencies to tarball.

Merged from OSS 3.1.x:

- [bug] JAVA-1371: Reintroduce connection pool timeout.

Cherry-picked from OSS 3.x:

- [new feature] JAVA-1248: Implement "beta" flag for native protocol v5.
- [improvement] JAVA-1367: Make protocol negotiation more resilient.


### 1.2.0-eap3

- [new feature] JAVA-1347: Add support for duration type.
- [bug] JAVA-1358: Add getCenter/getRadius to Distance.
- [improvement] JAVA-1354: Expose workload set from DSE node metadata.
- [new feature] JAVA-1343: Add Time() and Date() types for Graph.
- [new feature] JAVA-1338: Add new `fuzzy/tokenFuzzy` and `phrase` predicates in Graph.


### 1.2.0-eap2

- [improvement] JAVA-1335: Show Row-Level Access Control in DSE CQL metadata.
- [bug] JAVA-1330: Add un/register for SchemaChangeListener in DelegatingCluster
- [bug] JAVA-1351: Include Custom Payload in Request.copy.
- [improvement] JAVA-1264: Provide support for ProxyAuthentication.
- [bug] JAVA-1341: Handle null row in GraphSON 2.0 results


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
