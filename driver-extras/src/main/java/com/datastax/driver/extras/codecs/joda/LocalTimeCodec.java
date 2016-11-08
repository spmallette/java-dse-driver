/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.extras.codecs.joda;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ParseUtils;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.exceptions.InvalidTypeException;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.nio.ByteBuffer;

import static com.datastax.driver.core.ParseUtils.quote;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * {@link TypeCodec} that maps {@link LocalTime} to CQL {@code time} (long representing nanoseconds since midnight).
 * <p/>
 * <strong>IMPORTANT</strong>: {@link LocalTime} has millisecond precision; nanoseconds below one millisecond will be lost
 * during deserialization.
 */
public class LocalTimeCodec extends TypeCodec<LocalTime> {

    public static final LocalTimeCodec instance = new LocalTimeCodec();

    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("HH:mm:ss.SSS");

    private LocalTimeCodec() {
        super(DataType.time(), LocalTime.class);
    }

    @Override
    public ByteBuffer serialize(LocalTime value, ProtocolVersion protocolVersion) throws InvalidTypeException {
        if (value == null)
            return null;
        return bigint().serializeNoBoxing(MILLISECONDS.toNanos(value.getMillisOfDay()), protocolVersion);
    }

    @Override
    public LocalTime deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) throws InvalidTypeException {
        if (bytes == null || bytes.remaining() == 0)
            return null;
        long nanosOfDay = bigint().deserializeNoBoxing(bytes, protocolVersion);
        return LocalTime.fromMillisOfDay(NANOSECONDS.toMillis(nanosOfDay));
    }

    @Override
    public String format(LocalTime value) {
        if (value == null)
            return "NULL";
        return quote(FORMATTER.print(value));
    }

    @Override
    public LocalTime parse(String value) {
        if (value == null || value.isEmpty() || value.equalsIgnoreCase("NULL"))
            return null;

        // enclosing single quotes required, even for long literals
        if (!ParseUtils.isQuoted(value))
            throw new InvalidTypeException("time values must be enclosed by single quotes");
        value = value.substring(1, value.length() - 1);

        if (ParseUtils.isLongLiteral(value)) {
            long nanosOfDay;
            try {
                nanosOfDay = Long.parseLong(value);
            } catch (NumberFormatException e) {
                throw new InvalidTypeException(String.format("Cannot parse time value from \"%s\"", value), e);
            }
            return LocalTime.fromMillisOfDay(NANOSECONDS.toMillis(nanosOfDay));
        }

        try {
            return LocalTime.parse(value);
        } catch (RuntimeException e) {
            throw new InvalidTypeException(String.format("Cannot parse time value from \"%s\"", value), e);
        }
    }

}
