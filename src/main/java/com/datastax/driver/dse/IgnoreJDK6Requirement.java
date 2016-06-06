/*
 *      Copyright (C) 2012-2016 DataStax Inc.
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
