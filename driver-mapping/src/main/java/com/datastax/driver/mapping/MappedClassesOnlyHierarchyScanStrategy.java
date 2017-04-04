/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.mapping;

import java.util.Collections;
import java.util.List;

/**
 * A {@link HierarchyScanStrategy} that excludes all ancestors of mapped classes, thus
 * restricting class scan to the mapped classes themselves.
 * <p>
 * This strategy can be used instead of {@link DefaultHierarchyScanStrategy} to
 * achieve pre-<a href="https://datastax-oss.atlassian.net/browse/JAVA-541">JAVA-541</a>
 * behavior.
 */
public class MappedClassesOnlyHierarchyScanStrategy implements HierarchyScanStrategy {

    @Override
    public List<Class<?>> filterClassHierarchy(Class<?> mappedClass) {
        return Collections.<Class<?>>singletonList(mappedClass);
    }
}
