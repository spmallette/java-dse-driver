/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.JsonParserSequence;

import java.io.IOException;

/**
 * Utility class to easily concatenate multiple JsonParsers. This class had to be implemented because the class it is
 * extending, {@link JsonParserSequence}, inevitably skips a token when switching from one empty parser to a new one.
 * I.e. it is automatically calling {@link JsonParser#nextToken()} when switching to the new parser, ignoring
 * the current token.
 *
 * This class is used for better performance in GraphSON when trying to detect types.
 */
class JsonParserConcat extends JsonParserSequence {

    @SuppressWarnings("deprecation")
    JsonParserConcat(final JsonParser[] parsers) {
        super(parsers);
    }

    @Override
    public JsonToken nextToken() throws IOException {
        JsonToken t = this.delegate.nextToken();
        if (t != null) {
            return t;
        } else {
            do {
                if (!this.switchToNext()) {
                    return null;
                }
                // call getCurrentToken() instead of nextToken() in JsonParserSequence.
                t = this.delegate.getCurrentToken() == null
                        ? this.nextToken()
                        : this.getCurrentToken();
            } while (t == null);

            return t;
        }
    }
}
