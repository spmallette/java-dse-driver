/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import java.util.Map;

/**
 * Parent class for {@link Authenticator} implementations that support native protocol v1 authentication.
 * <p/>
 * Protocol v1 uses simple, credentials-based authentication (as opposed to SASL for later protocol versions).
 * In order to support protocol v1, an authenticator must extend this class.
 * <p/>
 * We use an abstract class instead of an interface because we don't want to expose {@link #getCredentials()}.
 *
 * @see <a href="https://github.com/apache/cassandra/blob/trunk/doc/native_protocol_v1.spec">Native protocol v1 specification</a>
 */
abstract class ProtocolV1Authenticator {
    abstract Map<String, String> getCredentials();
}
