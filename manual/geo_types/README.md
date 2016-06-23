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
