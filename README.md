# Java driver for DataStax Enterprise

This driver is built on top of the DataStax driver for Apache Cassandra, with specific extensions for DSE:

* [`Authenticator` implementations](manual/auth/) that use the authentication scheme negotiation in the server-side
  `DseAuthenticator`;
* value classes for [geospatial types](manual/geo_types/), and type codecs that integrate them seamlessly with the
  driver;
* [DSE graph integration](manual/graph/).

[core]: http://docs.datastax.com/en/developer/java-driver/3.1/

Note: since this driver is designed as a complimentary wrapper of the DataStax core driver for Apache Cassandra, a lot
of underlying concepts in this driver are the same, and to keep this documentation focused we will be linking to the
relevant section of the [core manual][core] where necessary.

*The Java DataStax Enterprise Driver can be used solely with DataStax Enterprise. Please consult
[the license](#license).*


## Getting the driver

The driver is available from Maven central:

```xml
<dependency>
  <groupId>com.datastax.cassandra</groupId>
  <artifactId>dse-driver</artifactId>
  <version>1.2.0</version>
</dependency>
```

## Reporting issues

Create a [JIRA](https://datastax-oss.atlassian.net/browse/JAVA) ticket with the "Component/s" field set to "DSE" (or contact DataStax support if you are a DSE customer).

## License

Copyright (C) 2012-2016 DataStax Inc.

The full license terms are available at http://www.datastax.com/terms/datastax-dse-driver-license-terms
