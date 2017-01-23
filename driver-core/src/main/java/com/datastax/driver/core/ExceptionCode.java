/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import com.datastax.driver.core.exceptions.DriverInternalError;

import java.util.HashMap;
import java.util.Map;

/**
 * Exceptions code, as defined by the native protocol.
 */
enum ExceptionCode {

    SERVER_ERROR(0x0000),
    PROTOCOL_ERROR(0x000A),

    BAD_CREDENTIALS(0x0100),

    // 1xx: problem during request execution
    UNAVAILABLE(0x1000),
    OVERLOADED(0x1001),
    IS_BOOTSTRAPPING(0x1002),
    TRUNCATE_ERROR(0x1003),
    WRITE_TIMEOUT(0x1100),
    READ_TIMEOUT(0x1200),
    READ_FAILURE(0x1300),
    FUNCTION_FAILURE(0x1400),
    WRITE_FAILURE(0x1500),

    // 2xx: problem validating the request
    SYNTAX_ERROR(0x2000),
    UNAUTHORIZED(0x2100),
    INVALID(0x2200),
    CONFIG_ERROR(0x2300),
    ALREADY_EXISTS(0x2400),
    UNPREPARED(0x2500),

    // 8xx: private failure codes
    CLIENT_WRITE_FAILURE(0x8000);

    public final int value;
    private static final Map<Integer, ExceptionCode> valueToCode = new HashMap<Integer, ExceptionCode>(ExceptionCode.values().length);

    static {
        for (ExceptionCode code : ExceptionCode.values())
            valueToCode.put(code.value, code);
    }

    ExceptionCode(int value) {
        this.value = value;
    }

    public static ExceptionCode fromValue(int value) {
        ExceptionCode code = valueToCode.get(value);
        if (code == null)
            throw new DriverInternalError(String.format("Unknown error code %d", value));
        return code;
    }
}
