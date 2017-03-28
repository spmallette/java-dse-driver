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


### Proxy authentication

DSE allows a user to connect as another user or role:

```
-- Allow bob to connect as alice:
GRANT PROXY.LOGIN ON ROLE 'alice' TO 'bob'
```

Once connected, all authorization checks will be performed against the proxy role (alice in this example).

To use proxy authentication with the driver, you need to provide the *authorization id*, in other 
words the name of the role you want to connect as.

With the plain text provider, pass the authorization id as a third parameter to the constructor: 

```java
AuthProvider authProvider = new DsePlainTextAuthProvider(
    "bob",
    "bob's password", 
    "alice");
```

With the GSSAPI (Kerberos) provider, use the corresponding builder method:  

```java
AuthProvider authProvider = DseGSSAPIAuthProvider.builder()
    .withLoginConfiguration(/* login configuration for Bob */)
    .withAuthorizationId("alice")
    .build();
```

### Proxy execution

Proxy execution is similar to proxy authentication, but it applies to a single query, not the whole session.

```
-- Allow bob to execute queries as alice:
GRANT PROXY.EXECUTE ON ROLE 'alice' TO 'bob'
```

Connect normally (without providing an authorization id); then specify the authorization id on the statement:

```java
Statement statement = new SimpleStatement("some query");
session.execute(statement.executingAs("alice"));
```

Note: statements are mutable, `executingAs` sets the authorization id on the original instance.