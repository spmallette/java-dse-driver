# Java driver for DataStax Enterprise

This driver is built on top of the DataStax driver for Apache Cassandra, with specific extensions for DSE:

* [`Authenticator` implementations](manual/auth/) that use the authentication scheme negotiation in the server-side
  `DseAuthenticator`;
* value classes for [geospatial types](manual/geo_types/), and type codecs that integrate them seamlessly with the
  driver;
* [DSE graph integration](manual/graph/).

[core]: http://datastax.github.io/java-driver/

Note: since this driver is designed as a complimentary wrapper of the DataStax core driver for Apache Cassandra, a lot
of underlying concepts in this driver are the same, and to keep this documentation focused we will be linking to the
relevant section of the [core manual][core] where necessary.

*The Java DataStax Enterprise Driver can be used solely with DataStax Enterprise. Please consult
[the license](#license).*


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


## License

Copyright (C) 2012-2016 DataStax Inc.

The full license terms are available at http://www.datastax.com/terms/datastax-dse-driver-license-terms
