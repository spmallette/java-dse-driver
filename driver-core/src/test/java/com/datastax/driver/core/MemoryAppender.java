/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

import java.io.StringWriter;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Simple Log4J appender that captures logs to memory in order to inspect them in unit tests.
 * <p/>
 * There is no purging mechanism, so make sure it doesn't stay enabled for too long (this is best
 * done with an {@code @After} method that removes it).
 */
public class MemoryAppender extends WriterAppender {

    private final Lock appendLock = new ReentrantLock();

    private final Condition append = appendLock.newCondition();

    public final StringWriter writer = new StringWriter();

    private int nextLogIdx = 0;

    public MemoryAppender() {
        setWriter(writer);
        setLayout(new PatternLayout("%m%n"));
    }

    @Override
    protected void subAppend(LoggingEvent event) {
        appendLock.lock();
        try {
            super.subAppend(event);
            append.signal();
        } finally {
            appendLock.unlock();
        }
    }

    public String get() {
        return writer.toString();
    }

    /**
     * Wait until at least one log event is appended to the current appender,
     * or a timeout occurs, whichever happens first,
     * then return the appender contents.
     * Not thread safe.
     * Useful when asynchronous code needs to wait until
     * the appender is actually invoked at least once.
     *
     * @param timeoutMillis timeout in milliseconds
     * @return The appender contents. Not thread safe.
     */
    public String waitAndGet(long timeoutMillis) throws InterruptedException {
        long nanos = MILLISECONDS.toNanos(timeoutMillis);
        appendLock.lock();
        try {
            while (get().isEmpty()) {
                if (nanos <= 0L) break; // timeout
                nanos = append.awaitNanos(nanos);
            }
            return get();
        } finally {
            appendLock.unlock();
        }
    }

    /**
     * @return The next set of logs after getNext was last called.  Not thread safe.
     */
    public String getNext() {
        String next = get().substring(nextLogIdx);
        nextLogIdx += next.length();
        return next;
    }

    public MemoryAppender enableFor(Class<?>... loggers) {
        for (Class<?> logger : loggers) {
            enableFor(logger.getName());
        }
        return this;
    }

    public MemoryAppender enableFor(org.slf4j.Logger... loggers) {
        for (org.slf4j.Logger logger : loggers) {
            enableFor(logger.getName());
        }
        return this;
    }

    public MemoryAppender enableFor(String... loggers) {
        for (String logger : loggers) {
            Logger log4jLogger = Logger.getLogger(logger);
            log4jLogger.addAppender(this);
        }
        return this;
    }

    public MemoryAppender disableFor(Class<?>... loggers) {
        for (Class<?> logger : loggers) {
            disableFor(logger.getName());
        }
        return this;
    }

    public MemoryAppender disableFor(org.slf4j.Logger... loggers) {
        for (org.slf4j.Logger logger : loggers) {
            disableFor(logger.getName());
        }
        return this;
    }

    public MemoryAppender disableFor(String... loggers) {
        for (String logger : loggers) {
            Logger log4jLogger = Logger.getLogger(logger);
            log4jLogger.removeAppender(this);
        }
        return this;
    }

}
