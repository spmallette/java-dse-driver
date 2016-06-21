## Upgrade guide

### cassandra-driver-dse-* to dse-driver-1.0.0

For previous versions of DSE, the driver extensions were published as a module of the core driver, under the coordinates
`com.datastax.cassandra:cassandra-driver-dse`. Starting with DSE 5, they become a standalone project:
`com.datastax.cassandra:dse-driver`. By separating the two projects, our goal is to allow separate lifecycles (for
example, we can release a patch version only for `dse-driver` if no core changes are needed).

In addition, we are switching to [semantic versioning] for the new project: each release number will now clearly express
the nature of the changes it contains (patches, new features or breaking changes). Since version numbers are strictly
codified by semver, following DSE server versioning is not possible; to make it clear that the two versioning schemes
are independent, we start the new driver project at 1.0.0.

[semantic versioning]: http://semver.org/

From an API perspective, `dse-driver` brings the following changes:

#### Dedicated cluster and session wrappers

The DSE driver now uses dedicated extensions of the core driver types: `DseCluster` and `DseSession`. Their main
advantage is to allow direct execution of [graph](../manual/graph/) statements.

See the root section of the [manual](../manual/) for more details.

#### Retries of idempotent statements

Historically, the driver retried failed queries indiscriminately. In recent versions of the core driver, the
[Statement#isIdempotent][idempotence] flag was introduced, to mark statements that are unsafe to retry when there is a
chance that they might have been applied already by a replica. To keep backward compatibility with previous versions,
the driver still retried these statements by default, and you had to configure a special retry policy to avoid retrying
them.

Starting with dse-driver-1.0.0, it is now the default behavior to **not retry** non-idempotent statements on write
timeouts or request errors. To help with the transition, a warning will be logged when the driver initializes, and the
first time a retry is aborted because of the `isIdempotent` flag (this warning will be removed in a future version).

Note that the driver does not position the `isIdempotent` flag automatically. Because it does not parse query strings,
it cannot determine if a particular query is idempotent or not. Therefore it takes a cautious approach and marks all
statements as non-idempotent by default. It is up to you to set the flag in your code if you know that your queries are
safe to retry.

Note that this behavior will also become the default in version 3.1.0 of the core driver.

[idempotence]: http://datastax.github.io/java-driver/manual/idempotence/
