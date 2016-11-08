/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Simple utility class to make sure we don't let exception slip away and kill
// our executors.
abstract class ExceptionCatchingRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionCatchingRunnable.class);

    public abstract void runMayThrow() throws Exception;

    @Override
    public void run() {
        try {
            runMayThrow();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Unexpected error while executing task", e);
        }
    }
}
