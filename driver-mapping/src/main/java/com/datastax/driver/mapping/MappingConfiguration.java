/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.mapping;

/**
 * The configuration to use for the mappers.
 */
public class MappingConfiguration {

    /**
     * Returns a new {@link Builder} instance.
     *
     * @return a new {@link Builder} instance.
     */
    public static MappingConfiguration.Builder builder() {
        return new MappingConfiguration.Builder();
    }

    /**
     * Builder for {@link MappingConfiguration} instances.
     */
    public static class Builder {

        private PropertyMapper propertyMapper = new DefaultPropertyMapper();

        /**
         * Sets the {@link PropertyMapper property access strategy} to use.
         *
         * @param propertyMapper the {@link PropertyMapper property access strategy} to use.
         * @return this {@link Builder} instance (to allow for fluent builder pattern).
         */
        public Builder withPropertyMapper(PropertyMapper propertyMapper) {
            this.propertyMapper = propertyMapper;
            return this;
        }

        /**
         * Builds a new instance of {@link MappingConfiguration} with this builder's
         * settings.
         *
         * @return a new instance of {@link MappingConfiguration}
         */
        public MappingConfiguration build() {
            return new MappingConfiguration(propertyMapper);
        }
    }

    private final PropertyMapper propertyMapper;

    private MappingConfiguration(PropertyMapper propertyMapper) {
        this.propertyMapper = propertyMapper;
    }

    /**
     * Returns the {@link PropertyMapper}.
     *
     * @return the {@link PropertyMapper}.
     */
    public PropertyMapper getPropertyMapper() {
        return propertyMapper;
    }

}
