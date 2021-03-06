/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.osgi;

import com.datastax.driver.osgi.api.MailboxException;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.datastax.driver.osgi.BundleOptions.*;
import static org.ops4j.pax.exam.CoreOptions.options;

@Listeners({CCMBridgeListener.class, PaxExam.class})
public class MailboxServiceHdrHistogramIT extends MailboxServiceTests {

    @Configuration
    public Option[] hdrHistogramConfig() {
        return options(
                defaultOptions(),
                hdrHistogramBundle(),
                nettyBundles(),
                guavaBundle(),
                extrasBundle(),
                mappingBundle(),
                driverBundle(),
                mailboxBundle()
        );
    }

    /**
     * Exercises a 'mailbox' service provided by an OSGi bundle that depends on the driver with
     * LZ4 compression activated.
     *
     * @test_category packaging
     * @expected_result Can create, retrieve and delete data using the mailbox service.
     * @jira_ticket JAVA-1200
     * @since 3.1.0
     */
    @Test(groups = "short")
    public void test_hdr() throws MailboxException {
        checkService();
    }
}
