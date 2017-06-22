/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.search;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ParseUtils;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.datastax.driver.dse.search.DateRange.DateRangeBound;
import com.datastax.driver.dse.search.DateRange.DateRangeBound.Precision;

import java.nio.ByteBuffer;
import java.util.Date;

import static com.datastax.driver.dse.search.DateRange.DateRangeBound.UNBOUNDED;

/**
 * A {@link TypeCodec codec} for {@link DateRange} instances.
 *
 * @since DSE 5.1
 */
public class DateRangeCodec extends TypeCodec<DateRange> {

    /**
     * The name of the server-side type handled by this codec.
     */
    public static final String CLASS_NAME = "org.apache.cassandra.db.marshal.DateRangeType";

    /**
     * The {@link DataType} handled by this codec.
     */
    public static final DataType.CustomType DATA_TYPE = DataType.custom(CLASS_NAME);

    /**
     * The singleton instance of {@link DateRangeCodec}.
     */
    public static final DateRangeCodec INSTANCE = new DateRangeCodec();

    // e.g. [2001-01-01]
    private final static byte DATE_RANGE_TYPE_SINGLE_DATE = 0x00;
    // e.g. [2001-01-01 TO 2001-01-31]
    private final static byte DATE_RANGE_TYPE_CLOSED_RANGE = 0x01;
    // e.g. [2001-01-01 TO *]
    private final static byte DATE_RANGE_TYPE_OPEN_RANGE_HIGH = 0x02;
    // e.g. [* TO 2001-01-01]
    private final static byte DATE_RANGE_TYPE_OPEN_RANGE_LOW = 0x03;
    // [* TO *]
    private final static byte DATE_RANGE_TYPE_BOTH_OPEN_RANGE = 0x04;
    // *
    private final static byte DATE_RANGE_TYPE_SINGLE_DATE_OPEN = 0x05;

    private DateRangeCodec() {
        super(DATA_TYPE, DateRange.class);
    }

    @Override
    public ByteBuffer serialize(DateRange dateRange, ProtocolVersion protocolVersion) throws InvalidTypeException {
        if (dateRange == null) {
            return null;
        }
        byte rangeType = encodeType(dateRange);
        int bufferSize = 1;
        if (!dateRange.getLowerBound().isUnbounded()) {
            bufferSize += 9;
        }
        if (!dateRange.isSingleBounded() && !dateRange.getUpperBound().isUnbounded()) {
            bufferSize += 9;
        }
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.put(rangeType);
        DateRangeBound lowerBound = dateRange.getLowerBound();
        if (!lowerBound.isUnbounded()) {
            buffer.putLong(lowerBound.getTimestamp().getTime());
            buffer.put(lowerBound.getPrecision().encoding);
        }
        if (!dateRange.isSingleBounded()) {
            DateRangeBound upperBound = dateRange.getUpperBound();
            if (!upperBound.isUnbounded()) {
                buffer.putLong(upperBound.getTimestamp().getTime());
                buffer.put(upperBound.getPrecision().encoding);
            }
        }
        return (ByteBuffer) buffer.flip();
    }

    @Override
    public DateRange deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) throws InvalidTypeException {
        if (bytes == null || bytes.remaining() == 0) {
            return null;
        }
        byte type = bytes.get();
        switch (type) {
            case DATE_RANGE_TYPE_SINGLE_DATE:
                return new DateRange(deserializeDateRangeLowerBound(bytes));
            case DATE_RANGE_TYPE_CLOSED_RANGE:
                return new DateRange(deserializeDateRangeLowerBound(bytes), deserializeDateRangeUpperBound(bytes));
            case DATE_RANGE_TYPE_OPEN_RANGE_HIGH:
                return new DateRange(deserializeDateRangeLowerBound(bytes), UNBOUNDED);
            case DATE_RANGE_TYPE_OPEN_RANGE_LOW:
                return new DateRange(UNBOUNDED, deserializeDateRangeUpperBound(bytes));
            case DATE_RANGE_TYPE_BOTH_OPEN_RANGE:
                return new DateRange(UNBOUNDED, UNBOUNDED);
            case DATE_RANGE_TYPE_SINGLE_DATE_OPEN:
                return new DateRange(UNBOUNDED);
        }
        throw new InvalidTypeException("Unknown date range type: " + type);
    }

    @Override
    public DateRange parse(String value) throws InvalidTypeException {
        if (value == null || value.isEmpty() || value.equalsIgnoreCase("NULL")) {
            return null;
        }
        try {
            return DateRange.parse(ParseUtils.unquote(value));
        } catch (Exception e) {
            throw new InvalidTypeException(String.format("Invalid date range literal: %s", value), e);
        }
    }

    @Override
    public String format(DateRange dateRange) throws InvalidTypeException {
        return dateRange == null ? "NULL" : ParseUtils.quote(dateRange.toString());
    }

    private byte encodeType(DateRange dateRange) {
        if (dateRange.isSingleBounded()) {
            return dateRange.getLowerBound().isUnbounded()
                    ? DATE_RANGE_TYPE_SINGLE_DATE_OPEN
                    : DATE_RANGE_TYPE_SINGLE_DATE;
        } else {
            if (dateRange.getLowerBound().isUnbounded()) {
                return dateRange.getUpperBound().isUnbounded()
                        ? DATE_RANGE_TYPE_BOTH_OPEN_RANGE
                        : DATE_RANGE_TYPE_OPEN_RANGE_LOW;
            } else {
                return dateRange.getUpperBound().isUnbounded()
                        ? DATE_RANGE_TYPE_OPEN_RANGE_HIGH
                        : DATE_RANGE_TYPE_CLOSED_RANGE;
            }
        }
    }

    private DateRangeBound deserializeDateRangeLowerBound(ByteBuffer bytes) {
        long epochMillis = bytes.getLong();
        Precision precision = Precision.fromEncoding(bytes.get());
        return DateRangeBound.lowerBound(new Date(epochMillis), precision);
    }

    private DateRangeBound deserializeDateRangeUpperBound(ByteBuffer bytes) {
        long epochMillis = bytes.getLong();
        Precision precision = Precision.fromEncoding(bytes.get());
        return DateRangeBound.upperBound(new Date(epochMillis), precision);
    }

}
