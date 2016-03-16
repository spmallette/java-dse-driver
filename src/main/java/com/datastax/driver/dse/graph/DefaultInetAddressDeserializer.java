/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 */
package com.datastax.driver.dse.graph;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.net.InetAddresses;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Default deserializer used by the driver for {@link InetAddress} instances.
 * The actual subclass returned by this deserializer depends on the type of
 * address: {@link java.net.Inet4Address IPV4} or {@link java.net.Inet6Address IPV6}.
 */
class DefaultInetAddressDeserializer<T extends InetAddress> extends StdDeserializer<T> {

    private final Class<T> inetClass;

    DefaultInetAddressDeserializer(Class<T> inetClass) {
        super(inetClass);
        this.inetClass = inetClass;
    }

    @Override
    public T deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        JsonLocation currentLocation = parser.getCurrentLocation();
        String ip = parser.readValueAs(String.class);
        try {
            InetAddress inet = InetAddresses.forString(ip);
            return inetClass.cast(inet);
        } catch (ClassCastException e) {
            throw new JsonParseException(String.format("Inet address cannot be cast to %s: %s", inetClass.getSimpleName(), ip), currentLocation, e);
        } catch (IllegalArgumentException e) {
            throw new JsonParseException(String.format("Expected inet address, got %s", ip), currentLocation, e);
        }
    }

}
