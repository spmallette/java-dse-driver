/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.mapping;

import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.UserType;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Serializes a class annotated with {@code @UDT} to the corresponding CQL user-defined type.
 */
class MappedUDTCodec<T> extends TypeCodec.AbstractUDTCodec<T> {
    private final UserType cqlUserType;
    private final Class<T> udtClass;
    private final Map<String, PropertyMapper> columnMappers;
    private final CodecRegistry codecRegistry;

    MappedUDTCodec(UserType cqlUserType, Class<T> udtClass, Map<String, PropertyMapper> columnMappers, MappingManager mappingManager) {
        super(cqlUserType, udtClass);
        this.cqlUserType = cqlUserType;
        this.udtClass = udtClass;
        this.columnMappers = columnMappers;
        this.codecRegistry = mappingManager.getSession().getCluster().getConfiguration().getCodecRegistry();
    }

    @Override
    protected T newInstance() {
        return ReflectionUtils.newInstance(udtClass);
    }

    Class<T> getUdtClass() {
        return udtClass;
    }

    @Override
    protected ByteBuffer serializeField(T source, String fieldName, ProtocolVersion protocolVersion) {
        PropertyMapper propertyMapper = columnMappers.get(fieldName);

        if (propertyMapper == null)
            return null;

        Object value = propertyMapper.getValue(source);

        TypeCodec<Object> codec = propertyMapper.customCodec;
        if (codec == null)
            codec = codecRegistry.codecFor(cqlUserType.getFieldType(propertyMapper.columnName), propertyMapper.javaType);

        return codec.serialize(value, protocolVersion);
    }

    @Override
    protected T deserializeAndSetField(ByteBuffer input, T target, String fieldName, ProtocolVersion protocolVersion) {
        PropertyMapper propertyMapper = columnMappers.get(fieldName);
        if (propertyMapper != null) {
            TypeCodec<Object> codec = propertyMapper.customCodec;
            if (codec == null)
                codec = codecRegistry.codecFor(cqlUserType.getFieldType(propertyMapper.columnName), propertyMapper.javaType);
            propertyMapper.setValue(target, codec.deserialize(input, protocolVersion));
        }
        return target;
    }

    @Override
    protected String formatField(T source, String fieldName) {
        PropertyMapper propertyMapper = columnMappers.get(fieldName);
        if (propertyMapper == null)
            return null;
        Object value = propertyMapper.getValue(source);
        TypeCodec<Object> codec = propertyMapper.customCodec;
        if (codec == null)
            codec = codecRegistry.codecFor(cqlUserType.getFieldType(propertyMapper.columnName), propertyMapper.javaType);
        return codec.format(value);
    }

    @Override
    protected T parseAndSetField(String input, T target, String fieldName) {
        PropertyMapper propertyMapper = columnMappers.get(fieldName);
        if (propertyMapper != null) {
            TypeCodec<Object> codec = propertyMapper.customCodec;
            if (codec == null)
                codec = codecRegistry.codecFor(cqlUserType.getFieldType(propertyMapper.columnName), propertyMapper.javaType);
            propertyMapper.setValue(target, codec.parse(input));
        }
        return target;
    }
}
