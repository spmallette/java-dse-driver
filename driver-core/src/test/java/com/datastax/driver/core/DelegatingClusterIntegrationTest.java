/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DelegatingClusterIntegrationTest extends CCMTestsSupport {

    @Test(groups = "short")
    public void should_allow_subclass_to_delegate_to_other_instance() {
        SimpleDelegatingCluster delegatingCluster = new SimpleDelegatingCluster(cluster());

        ResultSet rs = delegatingCluster.connect().execute("select * from system.local");

        assertThat(rs.all()).hasSize(1);
    }

    static class SimpleDelegatingCluster extends DelegatingCluster {

        private final Cluster delegate;

        public SimpleDelegatingCluster(Cluster delegate) {
            this.delegate = delegate;
        }

        @Override
        protected Cluster delegate() {
            return delegate;
        }
    }
}
