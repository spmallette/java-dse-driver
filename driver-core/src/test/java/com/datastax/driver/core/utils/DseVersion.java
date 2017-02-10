/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>Annotation for a Class or Method that defines a DataStax Enterprise Version requirement.
 * If the version in use does not meet the version requirement or DSE is not used, the test is skipped.</p>
 *
 * @see com.datastax.driver.core.TestListener#beforeInvocation(org.testng.IInvokedMethod, org.testng.ITestResult)
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DseVersion {

    /**
     * @return The minimum version required to execute this test, i.e. "2.0.13"
     */
    String value();

    /**
     * @return The description returned if this version requirement is not met.
     */
    String description() default "Does not meet minimum version requirement.";
}
