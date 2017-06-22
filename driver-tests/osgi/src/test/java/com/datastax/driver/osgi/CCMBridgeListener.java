/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.osgi;

import com.datastax.driver.core.CCMBridge;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * A listener that fires up a single node CCM instance on test class start and tears it
 * down on test class end.
 * <p/>
 * This is needed for tests that use Pax-Exam since it runs some methods in the OSGi container
 * which we do not want.
 */
public class CCMBridgeListener implements ITestListener {

    private CCMBridge ccm;

    @Override
    public void onStart(ITestContext context) {
        ccm = CCMBridge.builder().withNodes(1).withBinaryPort(9042).build();
    }

    @Override
    public void onFinish(ITestContext context) {
        if (ccm != null) {
            ccm.remove();
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
    }

    @Override
    public void onTestSuccess(ITestResult result) {
    }

    @Override
    public void onTestFailure(ITestResult result) {
    }

    @Override
    public void onTestSkipped(ITestResult result) {
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    }
}
