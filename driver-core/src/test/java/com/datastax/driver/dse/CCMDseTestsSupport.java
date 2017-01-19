/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.dse;

import com.datastax.driver.core.CCMTestsSupport;
import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.TestUtils;

public class CCMDseTestsSupport extends CCMTestsSupport {

    @Override
    public DseCluster.Builder createClusterBuilder() {
        return DseCluster.builder()
                .withCodecRegistry(new CodecRegistry())
                .withQueryOptions(TestUtils.nonDebouncingQueryOptions());
    }

    @Override
    public DseSession session() {
        return (DseSession) super.session();
    }

    @Override
    public DseCluster cluster() {
        return (DseCluster) super.cluster();
    }
}