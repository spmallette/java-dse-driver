/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.testng.annotations.DataProvider;

import java.util.Arrays;
import java.util.Iterator;

public class DataProviders {

    /**
     * @return A DataProvider that provides all non-serial consistency levels
     */
    @DataProvider(name = "consistencyLevels")
    public static Iterator<Object[]> consistencyLevels() {
        final Iterator<ConsistencyLevel> consistencyLevels = Iterables.filter(Arrays.asList(ConsistencyLevel.values()), new Predicate<ConsistencyLevel>() {
            @Override
            public boolean apply(ConsistencyLevel input) {
                // filter out serial CLs.
                return !input.isSerial();
            }
        }).iterator();

        return new Iterator<Object[]>() {

            @Override
            public boolean hasNext() {
                return consistencyLevels.hasNext();
            }

            @Override
            public Object[] next() {
                return new Object[]{
                        consistencyLevels.next()
                };
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("This shouldn't happen..");
            }
        };
    }

    /**
     * @return A DataProvider that provides all serial consistency levels
     */
    @DataProvider(name = "serialConsistencyLevels")
    public static Object[][] serialConsistencyLevels() {
        return new Object[][]{
                {ConsistencyLevel.SERIAL},
                {ConsistencyLevel.LOCAL_SERIAL}
        };
    }

}
