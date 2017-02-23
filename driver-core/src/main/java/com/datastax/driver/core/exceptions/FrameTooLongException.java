/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.exceptions;

/**
 * Indicates that the response frame for a request exceeded
 * {@link com.datastax.driver.core.Frame.Decoder.DecoderForStreamIdSize#MAX_FRAME_LENGTH}
 * (default: 256MB, configurable via com.datastax.driver.NATIVE_TRANSPORT_MAX_FRAME_SIZE_IN_MB
 * system property) and thus was not parsed.
 */
public class FrameTooLongException extends DriverException {

    private static final long serialVersionUID = 0;

    private final int streamId;

    public FrameTooLongException(int streamId) {
        this(streamId, null);
    }

    private FrameTooLongException(int streamId, Throwable cause) {
        super("Response frame exceeded maximum allowed length", cause);
        this.streamId = streamId;
    }

    /**
     * @return The stream id associated with the frame that caused this exception.
     */
    public int getStreamId() {
        return streamId;
    }

    @Override
    public FrameTooLongException copy() {
        return new FrameTooLongException(streamId, this);
    }
}
