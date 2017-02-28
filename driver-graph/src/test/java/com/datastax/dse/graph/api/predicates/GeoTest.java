/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.graph.api.predicates;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GeoTest {

    @Test(groups = "unit")
    public void testToDegrees() {

        assertThat(Geo.Unit.DEGREES.toDegrees(100.0)).isEqualTo(100.0);
        assertThat(Geo.Unit.MILES.toDegrees(68.9722)).isEqualTo(0.9982455747535043);
        assertThat(Geo.Unit.KILOMETERS.toDegrees(111.0)).isEqualTo(0.9982456082154465);
        assertThat(Geo.Unit.METERS.toDegrees(111000.0)).isEqualTo(0.9982456082154464);
    }
}
