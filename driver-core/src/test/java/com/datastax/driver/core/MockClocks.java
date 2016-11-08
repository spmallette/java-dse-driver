/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

class MockClocks {
    static class BackInTimeClock implements Clock {
        final long arbitraryTimeStamp = 1412610226270L;
        int calls;

        @Override
        public long currentTimeMicros() {
            return arbitraryTimeStamp - calls++;
        }
    }

    static class FixedTimeClock implements Clock {
        final long fixedTime;

        public FixedTimeClock(long fixedTime) {
            this.fixedTime = fixedTime;
        }

        @Override
        public long currentTimeMicros() {
            return fixedTime;
        }
    }
}
