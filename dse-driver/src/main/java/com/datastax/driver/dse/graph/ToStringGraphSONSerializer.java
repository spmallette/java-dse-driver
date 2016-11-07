/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse.graph;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.io.IOException;

/**
 * A different implementation of the {@link ToStringSerializer} that does not serialize types by calling
 * `typeSerializer.writeTypePrefixForScalar()` for unknown objects, because it doesn't make sense when there is a
 * custom types mechanism in place.
 *
 * @author Kevin Gallardo (https://kgdo.me)
 */
public class ToStringGraphSONSerializer extends ToStringSerializer {
    @Override
    public void serializeWithType(final Object value, final JsonGenerator gen, final SerializerProvider provider,
                                  final TypeSerializer typeSer) throws IOException {
        this.serialize(value, gen, provider);
    }

}
