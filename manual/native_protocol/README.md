## Native protocol

The native protocol defines the format of the binary messages exchanged
between the driver and DSE over TCP. As a driver user, you don't
need to know the fine details (although the protocol spec is [in the
DSE codebase][native_spec] if you're curious); the most visible
aspect is that some features are only available with specific protocol
versions.

[native_spec]: https://github.com/apache/cassandra/tree/trunk/doc

### Compatibility matrix

By default, the protocol version is negotiated between the driver and
DSE when the first connection is established. Both sides are
backward-compatible with older versions:

<table border="1" style="text-align:center; width:100%;margin-bottom:1em;">
<tr><td>&nbsp;</td><td>DSE: 3.2</td><td>4.0 to 4.6</td><td>4.7 to 4.8</td><td>5.0</td></tr>
<tr><td>Driver: 1.x</td> <td>v1</td> <td>v2</td>  <td>v3</td> <td>v4</td> </tr>
</table>

### Controlling the protocol version

To find out which version you're currently using, use
[ProtocolOptions#getProtocolVersion()][gpv]:

```java
ProtocolVersion myCurrentVersion = cluster.getConfiguration()
    .getProtocolOptions()
    .getProtocolVersion();
```

The protocol version can not be changed at runtime. However, you can
force a given version at initialization:

```java
DseCluster cluster = DseCluster.builder()
    .addContactPoint("127.0.0.1")
    .withProtocolVersion(ProtocolVersion.V2)
    .build();
```

If you specify a version that is not compatible with your current
driver/DSE combination, you'll get an error:

```
Exception in thread "main" com.datastax.driver.core.exceptions.NoHostAvailableException:
All host(s) tried for query failed
(tried: /127.0.0.1:9042 (com.datastax.driver.core.UnsupportedProtocolVersionException:
  [/127.0.0.1:9042] Host /127.0.0.1:9042 does not support protocol version V3 but V2))
```

[gpv]: http://docs.datastax.com/en/drivers/java-dse/1.1/com/datastax/driver/core/ProtocolOptions.html#getProtocolVersion--

#### Protocol version with mixed clusters

If you have a cluster with mixed versions (for example, while doing a
rolling upgrade of DSE), note that **the protocol version will be
negotiated with the first host the driver connects to**.

This could lead to the following situation (assuming you use driver 1.x):

* the first contact point is a DSE 5 host, so the driver negotiates
  protocol v4;
* while connecting to the rest of the cluster, the driver contacts a 4.8
  host using protocol v3, which fails; an error is logged and this host
  will be permanently ignored.

To avoid this issue, you can use one the following workarounds:

* always force a protocol version at startup. You keep it at v3 while
  the rolling upgrade is happening, and only switch to v4 when the whole
  cluster has switched to DSE 5;
* ensure that the list of initial contact points only contains hosts
  with the oldest version (4.8 in this example).


### New features by protocol version

#### v1 to v2

* bound variables in simple statements
  ([Session#execute(String, Object...)](http://docs.datastax.com/en/drivers/java-dse/1.1/com/datastax/driver/core/Session.html#execute-java.lang.String-java.lang.Object...-))
* [batch statements](http://docs.datastax.com/en/drivers/java-dse/1.1/com/datastax/driver/core/BatchStatement.html)
* [query paging](../paging/)

#### v2 to v3

* the number of stream ids per connection goes from 128 to 32768 (see
  [Connection pooling](../pooling/))
* [serial consistency on batch statements](http://docs.datastax.com/en/drivers/java-dse/1.1/com/datastax/driver/core/BatchStatement.html#setSerialConsistencyLevel-com.datastax.driver.core.ConsistencyLevel-)
* [client-side timestamps](../query_timestamps/)

#### v3 to v4

* [query warnings](http://docs.datastax.com/en/drivers/java-dse/1.1/com/datastax/driver/core/ExecutionInfo.html#getWarnings--)
* allowed unset values in bound statements
* [Custom payloads](../custom_payloads/)
