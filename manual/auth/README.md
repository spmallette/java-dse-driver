## Authentication

For clients connecting to a DSE cluster secured with `DseAuthenticator`, two authentication providers are included:

* `DsePlainTextAuthProvider`: plain-text authentication;
* `DseGSSAPIAuthProvider`: GSSAPI authentication.

To configure a provider, pass it when initializing the cluster:

```java
import com.datastax.driver.dse.auth.DseGSSAPIAuthProvider;

DseCluster dseCluster = DseCluster.builder()
        .addContactPoint("127.0.0.1")
        .withAuthProvider(new DseGSSAPIAuthProvider())
        .build();
```

See the Javadocs of each implementation for more details.
