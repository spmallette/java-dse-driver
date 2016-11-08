/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.exceptions;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.WriteType;

import java.net.InetSocketAddress;

/**
 * A non-timeout error during a write query.
 * <p/>
 * This happens when some of the replicas that were contacted by the coordinator replied with an error.
 */
@SuppressWarnings("serial")
public class WriteFailureException extends QueryConsistencyException {
    private final WriteType writeType;
    private final int failed;

    /**
     * This constructor should only be used internally by the driver
     * when decoding error responses.
     */
    public WriteFailureException(ConsistencyLevel consistency, WriteType writeType, int received, int required, int failed) {
        this(null, consistency, writeType, received, required, failed);
    }

    public WriteFailureException(InetSocketAddress address, ConsistencyLevel consistency, WriteType writeType, int received, int required, int failed) {
        super(address, String.format("Cassandra failure during write query at consistency %s "
                                + "(%d responses were required but only %d replica responded, %d failed)",
                        consistency, required, received, failed),
                consistency,
                received,
                required);
        this.writeType = writeType;
        this.failed = failed;
    }

    private WriteFailureException(InetSocketAddress address, String msg, Throwable cause,
                                  ConsistencyLevel consistency, WriteType writeType, int received, int required, int failed) {
        super(address, msg, cause, consistency, received, required);
        this.writeType = writeType;
        this.failed = failed;
    }

    /**
     * The type of the write for which a timeout was raised.
     *
     * @return the type of the write for which a timeout was raised.
     */
    public WriteType getWriteType() {
        return writeType;
    }

    /**
     * Returns the number of replicas that experienced a failure while executing the request.
     *
     * @return the number of failures.
     */
    public int getFailures() {
        return failed;
    }

    @Override
    public WriteFailureException copy() {
        return new WriteFailureException(getAddress(), getMessage(), this, getConsistencyLevel(), getWriteType(),
                getReceivedAcknowledgements(), getRequiredAcknowledgements(), getFailures());
    }

    public WriteFailureException copy(InetSocketAddress address) {
        return new WriteFailureException(
                address,
                getMessage(),
                this,
                getConsistencyLevel(),
                getWriteType(),
                getReceivedAcknowledgements(),
                getRequiredAcknowledgements(),
                failed);
    }
}
