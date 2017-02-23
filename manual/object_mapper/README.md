# Object Mapper

The Cassandra driver provides a simple object mapper, which
avoids most of the boilerplate when converting your domain classes to
and from query results. It handles basic CRUD operations in Cassandra tables
containing UDTs, collections and all native CQL types.

To use this module with the DSE driver, import the dependency but exclude the Cassandra driver (you're using the DSE
driver which is a drop-in replacement):

```xml
<dependency>
  <groupId>com.datastax.dse</groupId>
  <artifactId>dse-driver-mapping</artifactId>
  <version>3.1.2</version>
  <exclusions>
    <exclusion>
      <groupId>com.datastax.dse</groupId>
      <artifactId>dse-driver-core</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```

See the child pages for more information:

* [definition of mapped classes](creating/)
* [using the mapper](using/)
* [using custom codecs](custom_codecs/)
