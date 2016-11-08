/*
 *      Copyright (C) 2012-2016 DataStax Inc.
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
public class MailboxServiceGuava19IT extends MailboxServiceTests {

    @Configuration
    public Option[] guava19Config() {
        return options(
                defaultOptions(),
                nettyBundles(),
                guavaBundle().version("19.0"),
                driverBundle(),
                extrasBundle(),
                mappingBundle(),
                mailboxBundle()
        );
    }

    /**
     * Exercises a 'mailbox' service provided by an OSGi bundle that depends on the driver with
     * Guava 19 explicitly enforced.
     *
     * @test_category packaging
     * @expected_result Can create, retrieve and delete data using the mailbox service.
     * @jira_ticket JAVA-620
     * @since 2.0.10, 2.1.5
     */
    @Test(groups = "short")
    public void test_guava_19() throws MailboxException {
        checkService();
    }
}
