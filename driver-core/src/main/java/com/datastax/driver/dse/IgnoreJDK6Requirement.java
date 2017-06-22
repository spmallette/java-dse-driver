/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse;

/**
 * Annotation used to mark classes in this project as
 * excluded from JDK 6 signature check performed
 * by <a href="http://www.mojohaus.org/animal-sniffer/animal-sniffer-maven-plugin/check-mojo.html">animal-sniffer</a>
 * Maven plugin as they require JDK 8 or higher.
 */
public @interface IgnoreJDK6Requirement {
}
