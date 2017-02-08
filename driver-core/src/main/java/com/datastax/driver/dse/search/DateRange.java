/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.search;


import com.datastax.driver.core.ParseUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.datastax.driver.dse.search.DateRange.DateRangeBound.UNBOUNDED;

/**
 * A date range, as defined by the  C* type {@value DateRangeCodec#CLASS_NAME},
 * corresponding to the Apache Solr type
 * <a href="https://lucene.apache.org/solr/6_3_0/solr-core/index.html?org/apache/solr/schema/DateRangeField.html">{@code DateRangeField}</a>.
 * <p/>
 * A date range can be either {@link DateRange#DateRange(DateRangeBound)
 * single-bounded}, in which case it represents
 * a unique instant (e.g. "{@code 2001-01-01}"), or
 * {@link #DateRange(DateRangeBound, DateRangeBound) double-bounded},
 * in which case it represents an interval of time (e.g. "{@code [2001-01-01 TO 2002]}").
 * <p/>
 * Date range {@link DateRangeBound bounds} are always inclusive;
 * they must be either valid dates,
 * or the special value {@link DateRangeBound#UNBOUNDED UNBOUNDED}, represented
 * by a "{@code *}", e.g. "{@code [2001 TO *]}".
 * <p/>
 * {@link DateRange} instances can be more easily created with the
 * {@link #parse(String)} method.
 * <p/>
 * {@link DateRange} instances are immutable and thread-safe.
 *
 * @since DSE 5.1
 */
public class DateRange {

    /**
     * Parses the given string as a {@link DateRange}.
     * <p/>
     * The given input must be compliant with Apache Solr type
     * <a href="https://lucene.apache.org/solr/6_3_0/solr-core/index.html?org/apache/solr/schema/DateRangeField.html">{@code DateRangeField}</a> syntax;
     * it can either be a {@link #DateRange(DateRangeBound) single-bounded range},
     * or a {@link #DateRange(DateRangeBound, DateRangeBound) double-bounded range}.
     *
     * @param source The string to parse.
     * @return a {@link DateRange} object.
     * @throws ParseException if the given string could not be parsed into a valid {@link DateRange}.
     * @see DateRangeBound#parseLowerBound(String)
     * @see DateRangeBound#parseUpperBound(String)
     */
    public static DateRange parse(String source) throws ParseException {
        Preconditions.checkNotNull(source);
        if (DateRange.isUnbounded(source)) {
            return new DateRange(UNBOUNDED);
        }
        if (source.charAt(0) == '[') {
            if (source.charAt(source.length() - 1) != ']') {
                throw new ParseException("If date range starts with '[' it must end with ']'; got " + source, source.length() - 1);
            }
            int middle = source.indexOf(" TO ");
            if (middle < 0) {
                throw new ParseException("If date range starts with '[' it must contain ' TO '; got " + source, 0);
            }
            String lowerBoundStr = source.substring(1, middle);
            int upperBoundStart = middle + 4;
            String upperBoundStr = source.substring(upperBoundStart, source.length() - 1);
            DateRangeBound lowerBound;
            try {
                lowerBound = DateRangeBound.parseLowerBound(lowerBoundStr);
            } catch (Exception e) {
                throw new ParseException("Cannot parse date range lower bound: " + source, 1);
            }
            DateRangeBound upperBound;
            try {
                upperBound = DateRangeBound.parseUpperBound(upperBoundStr);
            } catch (Exception e) {
                throw new ParseException("Cannot parse date range upper bound: " + source, upperBoundStart);
            }
            return new DateRange(lowerBound, upperBound);
        } else {
            try {
                return new DateRange(DateRangeBound.parseLowerBound(source));
            } catch (Exception e) {
                throw new ParseException("Cannot parse single date range bound: " + source, 0);
            }
        }
    }

    private static boolean isUnbounded(String source) {
        return "*".equals(source);
    }

    private final DateRangeBound lowerBound;

    private final DateRangeBound upperBound;

    /**
     * Creates a "single bounded" {@link DateRange} instance, i.e., a date range whose upper and lower
     * bounds are identical.
     *
     * @param singleBound the single {@link DateRangeBound bound} of this range; must not be {@code null}.
     */
    public DateRange(DateRangeBound singleBound) {
        Preconditions.checkArgument(singleBound != null, "singleBound cannot be null");
        this.lowerBound = singleBound;
        this.upperBound = null;
    }

    /**
     * Create a {@link DateRange} composed of two distinct {@link DateRangeBound bounds}.
     *
     * @param lowerBound the lower bound of this range (inclusive); must not be {@code null}.
     * @param upperBound the upper bound of this range (inclusive); must not be {@code null}.
     * @throws IllegalArgumentException if {@code lowerBound} is {@code null}, if {@code upperBound} is {@code null},
     *                                  or if both {@code lowerBound} and {@code upperBound} are not unbounded and
     *                                  {@code lowerBound} is greater than {@code upperBound}.
     */
    public DateRange(DateRangeBound lowerBound, DateRangeBound upperBound) {
        Preconditions.checkArgument(lowerBound != null, "lowerBound cannot be null");
        Preconditions.checkArgument(upperBound != null, "upperBound cannot be null");
        if (!lowerBound.isUnbounded() && !upperBound.isUnbounded() && lowerBound.timestamp.after(upperBound.timestamp)) {
            throw new IllegalArgumentException(
                    String.format("Lower bound of a date range should be before upper bound, got: [%s TO %s]",
                            lowerBound,
                            upperBound));
        }
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    /**
     * Returns the lower {@link DateRangeBound bound} of this range (inclusive).
     *
     * @return the lower {@link DateRangeBound bound} of this range (inclusive).
     */
    public DateRangeBound getLowerBound() {
        return lowerBound;
    }

    /**
     * Returns the upper {@link DateRangeBound bound} of this range (inclusive).
     * <p/>
     * This method returns {@code null} for {@link #isSingleBounded() single-bounded ranges}
     * created via {@link #DateRange(DateRangeBound)}.
     *
     * @return the lower {@link DateRangeBound bound} of this range (inclusive).
     */
    public DateRangeBound getUpperBound() {
        return upperBound;
    }

    /**
     * Returns {@code true} if this {@link DateRange} is
     * a {@link #DateRange(DateRangeBound) single-bounded range},
     * and {@code false} if it is a
     * {@link #DateRange(DateRangeBound, DateRangeBound) double-bounded range}.
     *
     * @return {@code true} if this {@link DateRange} is a
     * {@link #DateRange(DateRangeBound) single-bounded range}, {@code false} otherwise.
     */
    public boolean isSingleBounded() {
        return upperBound == null;
    }

    /**
     * Returns the string representation of this range, in a format compatible with
     * <a href="https://cwiki.apache.org/confluence/display/solr/Working+with+Dates">Apache Solr DateRageField syntax</a>
     *
     * @return the string representation of this range.
     * @see DateRangeBound#toString()
     */
    @Override
    public String toString() {
        if (isSingleBounded()) {
            return lowerBound.toString();
        } else {
            return String.format("[%s TO %s]", lowerBound, upperBound);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DateRange dateRange = (DateRange) o;
        return lowerBound.equals(dateRange.lowerBound) && (upperBound != null
                ? upperBound.equals(dateRange.upperBound)
                : dateRange.upperBound == null);
    }

    @Override
    public int hashCode() {
        int result = lowerBound.hashCode();
        result = 31 * result + (upperBound != null ? upperBound.hashCode() : 0);
        return result;
    }

    /**
     * A date range bound.
     * <p/>
     * Date range bounds are composed of a {@link Date} field and
     * a corresponding {@link Precision}.
     * <p/>
     * Date range bounds are inclusive.
     * The special value {@link #UNBOUNDED} denotes an un unbounded (infinite) bound,
     * represented by a {@code *} sign.
     * <p/>
     * {@code DateRangeBound} instances are immutable and thread-safe.
     */
    public static class DateRangeBound {

        /**
         * The unbounded {@link DateRangeBound} instance. Unbounded bounds
         * are syntactically represented by a {@code *} (star) sign.
         */
        public static final DateRangeBound UNBOUNDED = new DateRangeBound();

        /**
         * Parses the given input as a lower date range bound.
         * <p/>
         * The input should be a <a
         * href="https://github.com/apache/lucene-solr/blob/releases/lucene-solr/6.4.0/lucene/spatial-extras/src/java/org/apache/lucene/spatial/prefix/tree/DateRangePrefixTree.java#L441"
         * >Lucene-compliant</a> string, but in practice, most ISO-8601 datetime formats are recognizable;
         * in particular, this method
         * accepts a variety of timezone formats, even if Lucene itself requires
         * timezones expressed solely as "Z" (i.e. UTC).
         * <p/>
         * The returned {@link DateRangeBound bound} will have its {@link Precision precision} inferred from
         * the input, and its timestamp will be {@link Precision#roundDown(Date) rounded down} to that precision.
         *
         * @param lowerBound The lower date range bound to parse; must not be {@code null}.
         * @return A {@link DateRangeBound}.
         * @throws IllegalArgumentException if {@code lowerBound} is {@code null}.
         * @throws ParseException           if the given input cannot be parsed.
         */
        public static DateRangeBound parseLowerBound(String lowerBound) throws ParseException {
            Preconditions.checkNotNull(lowerBound);
            if (DateRange.isUnbounded(lowerBound)) {
                return UNBOUNDED;
            }
            for (Precision precision : Precision.values()) {
                try {
                    Date timestamp = precision.parse(lowerBound);
                    return lowerBound(timestamp, precision);
                } catch (Exception ignored) {
                }
            }
            throw new ParseException("Cannot parse lower date range bound: " + lowerBound, 0);
        }

        /**
         * Parses the given input as an upper date range bound.
         * <p/>
         * The input should be a <a
         * href="https://github.com/apache/lucene-solr/blob/releases/lucene-solr/6.4.0/lucene/spatial-extras/src/java/org/apache/lucene/spatial/prefix/tree/DateRangePrefixTree.java#L441"
         * >Lucene-compliant</a> string, but in practice, most ISO-8601 datetime formats are recognizable;
         * in particular, this method
         * accepts a variety of timezone formats, even if Lucene itself requires
         * timezones expressed solely as "Z" (i.e. UTC).
         * <p/>
         * The returned {@link DateRangeBound bound} will have its {@link Precision precision} inferred from
         * the input, and its timestamp will be {@link Precision#roundUp(Date)} rounded up} to that precision.
         *
         * @param upperBound The upper date range bound to parse; must not be {@code null}.
         * @return A {@link DateRangeBound}.
         * @throws IllegalArgumentException if {@code upperBound} is {@code null}.
         * @throws ParseException           if the given input cannot be parsed.
         */
        public static DateRangeBound parseUpperBound(String upperBound) throws ParseException {
            Preconditions.checkNotNull(upperBound);
            if (DateRange.isUnbounded(upperBound)) {
                return UNBOUNDED;
            }
            for (Precision precision : Precision.values()) {
                try {
                    Date timestamp = precision.parse(upperBound);
                    return upperBound(timestamp, precision);
                } catch (Exception ignored) {
                }
            }
            throw new ParseException("Cannot parse upper date range bound: " + upperBound, 0);
        }

        /**
         * Creates a date range lower bound from the given {@link Date} and {@link Precision}.
         * Temporal fields smaller than the precision will be rounded down.
         *
         * @param timestamp The timestamp to use.
         * @param precision The precision to use.
         * @return A lower range bound.
         */
        public static DateRangeBound lowerBound(Date timestamp, Precision precision) {
            Date roundedLowerBound = precision.roundDown(timestamp);
            return new DateRangeBound(roundedLowerBound, precision);
        }

        /**
         * Creates a date range upper bound from the given {@link Date} and {@link Precision}.
         * Temporal fields smaller than the precision will be rounded up.
         *
         * @param timestamp The timestamp to use.
         * @param precision The precision to use.
         * @return An upper range bound.
         */
        public static DateRangeBound upperBound(Date timestamp, Precision precision) {
            Date roundedUpperBound = precision.roundUp(timestamp);
            return new DateRangeBound(roundedUpperBound, precision);
        }

        private final Date timestamp;

        private final Precision precision;

        private DateRangeBound(Date timestamp, Precision precision) {
            Preconditions.checkNotNull(timestamp);
            Preconditions.checkNotNull(precision);
            this.timestamp = timestamp;
            this.precision = precision;
        }

        // constructor used for the special UNBOUNDED value
        private DateRangeBound() {
            this.timestamp = null;
            this.precision = null;
        }

        /**
         * Whether this bound is an unbounded bound.
         *
         * @return Whether this bound is an unbounded bound.
         */
        public boolean isUnbounded() {
            return this == UNBOUNDED;
        }

        /**
         * Returns the timestamp of this bound.
         *
         * @return the timestamp of this bound.
         */
        public Date getTimestamp() {
            return timestamp;
        }

        /**
         * Returns the precision of this bound.
         *
         * @return the precision of this bound.
         */
        public Precision getPrecision() {
            return precision;
        }

        /**
         * Returns this {@link DateRangeBound bound} as a <a
         * href="https://github.com/apache/lucene-solr/blob/releases/lucene-solr/6.4.0/lucene/spatial-extras/src/java/org/apache/lucene/spatial/prefix/tree/DateRangePrefixTree.java#L363"
         * >Lucene-compliant</a> string.
         * <p/>
         * Unbounded bounds always return "{@code *}"; all other bounds
         * are formatted in one of the common ISO-8601 datetime formats,
         * depending on their precision.
         * <p/>
         * Note that Lucene expects timestamps in UTC only.
         * Timezone presence is always optional, and if present,
         * it must be expressed with the symbol "Z" exclusively.
         * Therefore this method does not include any timezone
         * information in the returned string, except for bounds with
         * {@link Precision#MILLISECOND millisecond} precision, where
         * the symbol "Z" is always appended to the resulting string.
         *
         * @return this {@link DateRangeBound} as a Lucene-compliant string.
         */
        @Override
        public String toString() {
            if (isUnbounded()) {
                return "*";
            }
            return precision.format(timestamp);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DateRangeBound that = (DateRangeBound) o;
            if (this.isUnbounded()) return that.isUnbounded();
            return this.timestamp.equals(that.timestamp) && this.precision == that.precision;
        }

        @Override
        public int hashCode() {
            if (this.isUnbounded()) return 0;
            int result = timestamp.hashCode();
            result = 31 * result + precision.hashCode();
            return result;
        }

        /**
         * The precision of a {@link DateRangeBound}.
         */
        public enum Precision {

            MILLISECOND((byte) 0x06,
                    "yyyy-MM-dd'T'HH:mm:ss.SSS",
                    "yyyy-MM-dd'T'HH:mm:ss.SSSZ"),

            SECOND((byte) 0x05,
                    "yyyy-MM-dd'T'HH:mm:ss",
                    "yyyy-MM-dd'T'HH:mm:ssZ"),

            MINUTE((byte) 0x04,
                    "yyyy-MM-dd'T'HH:mm",
                    "yyyy-MM-dd'T'HH:mmZ"),

            HOUR((byte) 0x03,
                    "yyyy-MM-dd'T'HH",
                    "yyyy-MM-dd'T'HHZ"),

            DAY((byte) 0x02,
                    "yyyy-MM-dd",
                    "yyyy-MM-ddZ"),

            MONTH((byte) 0x01,
                    "yyyy-MM",
                    "yyyy-MMZ"),

            YEAR((byte) 0x00,
                    "yyyy",
                    "yyyyZ");

            private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
            private static final Date MIN_DATE = new Date(Long.MIN_VALUE);

            final byte encoding;

            private final String[] patterns;

            Precision(byte encoding, String... patterns) {
                this.encoding = encoding;
                this.patterns = patterns;
            }

            private static final Map<Byte, Precision> ENCODINGS;

            static {
                ImmutableMap.Builder<Byte, Precision> builder = ImmutableMap.builder();
                for (Precision precision : values()) {
                    builder.put(precision.encoding, precision);
                }
                ENCODINGS = builder.build();
            }

            static Precision fromEncoding(byte encoding) {
                Precision precision = ENCODINGS.get(encoding);
                if (precision == null)
                    throw new IllegalArgumentException("Invalid precision encoding: " + encoding);
                return precision;
            }

            /**
             * Rounds up the given timestamp to this precision.
             * <p/>
             * Temporal fields smaller than this precision will be rounded up;
             * other fields will be left untouched.
             *
             * @param timestamp The timestamp to round up.
             * @return A rounded up timestamp according to this precision.
             */
            public Date roundUp(Date timestamp) {
                GregorianCalendar calendar = newProlepticCalendar(timestamp);
                switch (this) {
                    case YEAR:
                        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
                    case MONTH:
                        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    case DAY:
                        calendar.set(Calendar.HOUR_OF_DAY, 23);
                    case HOUR:
                        calendar.set(Calendar.MINUTE, 59);
                    case MINUTE:
                        calendar.set(Calendar.SECOND, 59);
                    case SECOND:
                        calendar.set(Calendar.MILLISECOND, 999);
                }
                // DateRangeField ignores any precision beyond milliseconds
                return calendar.getTime();
            }

            /**
             * Rounds down the given timestamp to this precision.
             * <p/>
             * Temporal fields smaller than this precision will be rounded down;
             * other fields will be left untouched.
             *
             * @param timestamp The timestamp to round down.
             * @return A rounded down timestamp according to this precision.
             */
            public Date roundDown(Date timestamp) {
                GregorianCalendar calendar = newProlepticCalendar(timestamp);
                switch (this) {
                    case YEAR:
                        calendar.set(Calendar.MONTH, Calendar.JANUARY);
                    case MONTH:
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                    case DAY:
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                    case HOUR:
                        calendar.set(Calendar.MINUTE, 0);
                    case MINUTE:
                        calendar.set(Calendar.SECOND, 0);
                    case SECOND:
                        calendar.set(Calendar.MILLISECOND, 0);
                }
                // DateRangeField ignores any precision beyond milliseconds
                return calendar.getTime();
            }

            /**
             * Parses the given input according to this precision, as a lower bound.
             * <p/>
             * The parsing is strict, i.e. if this precision is unable to fully parse
             * the input, it will throw an exception.
             *
             * @param source The input to parse
             * @return A {@link Date} representing the parsed input.
             * @throws ParseException if the input cannot be parsed with this precision.
             */
            private Date parse(String source) throws ParseException {
                for (String pattern : patterns) {
                    try {
                        // parser must be lenient to accept negative years
                        return ParseUtils.parseDate(source, pattern, true);
                    } catch (ParseException ignored) {
                    }
                }
                throw new ParseException("Unable to parse the date: " + source, 0);
            }

            /**
             * Formats the given timestamp according to this precision.
             *
             * @param timestamp The timestamp to format
             * @return A string representing the formatted timestamp.
             */
            private String format(Date timestamp) {
                GregorianCalendar cal = newProlepticCalendar(timestamp);
                // use primary format as the format pattern
                String pattern = patterns[0];
                // even if the calendar is proleptic,
                // we still need to manually format negative years,
                // as SimpleDateFormat would still use BC era
                // when formatting, which is not what we want here
                boolean isBeforeChrist = cal.get(Calendar.ERA) == GregorianCalendar.BC;
                if (isBeforeChrist) {
                    if (this == YEAR)
                        return formatAsProlepticYear(cal.get(Calendar.YEAR));
                    // remove "yyyy-" from the pattern for now, we'll deal with it later
                    pattern = pattern.substring(5);
                }
                SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.ROOT);
                formatter.setCalendar(cal);
                String formatted = formatter.format(timestamp);
                if (isBeforeChrist) {
                    formatted = formatAsProlepticYear(cal.get(Calendar.YEAR)) + '-' + formatted;
                }
                if (this == MILLISECOND)
                    formatted += "Z";
                return formatted;
            }

            /**
             * Lucene uses a proleptic gregorian calendar, so the
             * first year before 1 AD is not 1 BC, but 0;
             * the second before 1 AD is not 2 BC, but -1;
             * and so on.
             * <p/>
             * This method transforms gregorian calendar years
             * expressed in BC era into negative years
             * compliant with the proleptic gregorian calendar.
             *
             * @param yearBeforeChrist The year BC to format.
             * @return the given year formatter as a proleptic year.
             */
            private static String formatAsProlepticYear(int yearBeforeChrist) {
                int yearProleptic = -yearBeforeChrist + 1;
                if (yearProleptic == 0) {
                    return "0000";
                }
                return String.format("%05d", yearProleptic);
            }

            private static GregorianCalendar newProlepticCalendar(Date timestamp) {
                GregorianCalendar cal = new GregorianCalendar(UTC, Locale.ROOT);
                // make the formatter proleptic, see java.util.GregorianCalendar.from(ZonedDateTime)
                cal.setGregorianChange(MIN_DATE);
                cal.setFirstDayOfWeek(Calendar.MONDAY);
                cal.setMinimalDaysInFirstWeek(4);
                cal.setTime(timestamp);
                return cal;
            }
        }
    }

}
