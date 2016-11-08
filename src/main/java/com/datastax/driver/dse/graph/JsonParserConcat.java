/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.JsonParserSequence;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Utility class to easily concatenate multiple JsonParsers. This class had to be implemented because the class it is
 * extending, {@link JsonParserSequence}, inevitably skips a token when switching from one empty parser to a new one.
 * I.e. it is automatically calling {@link JsonParser#nextToken()} when switching to the new parser, ignoring
 * the current token.
 *
 * This class is used for better performance in GraphSON when trying to detect types.
 */
class JsonParserConcat extends JsonParserSequence {
    protected JsonParserConcat(final JsonParser[] parsers) {
        super(parsers);
    }

    public static JsonParserConcat createFlattened(final JsonParser first, final JsonParser second) {
        if (!(first instanceof JsonParserConcat) && !(second instanceof JsonParserConcat)) {
            return new JsonParserConcat(new JsonParser[]{first, second});
        } else {
            final ArrayList p = new ArrayList();
            if (first instanceof JsonParserConcat) {
                ((JsonParserConcat) first).addFlattenedActiveParsers(p);
            } else {
                p.add(first);
            }

            if (second instanceof JsonParserConcat) {
                ((JsonParserConcat) second).addFlattenedActiveParsers(p);
            } else {
                p.add(second);
            }
            return new JsonParserConcat((JsonParser[]) p.toArray(new JsonParser[p.size()]));
        }
    }

    @Override
    public JsonToken nextToken() throws IOException, JsonParseException {
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
