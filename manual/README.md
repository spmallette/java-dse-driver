## Manual

### Getting started

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

### More information

* [authentication](auth/)
* [geospatial types](geo_types/)
* [graph](graph/)
