/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.extras.codecs.jdk8;

/**
 * Annotation used to mark classes in this package as
 * excluded from JDK signature check performed
 * by <a href="http://www.mojohaus.org/animal-sniffer/animal-sniffer-maven-plugin/check-mojo.html">animal-sniffer</a>
 * Maven plugin as they require JDK 8 and not the usual JDK 6.
 */
@interface IgnoreJDK6Requirement {
}
