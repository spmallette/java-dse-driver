/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.osgi;

import com.datastax.driver.osgi.api.MailboxException;
import com.datastax.driver.osgi.api.MailboxMessage;
import com.datastax.driver.osgi.api.MailboxService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;

import static org.testng.Assert.assertEquals;

public abstract class MailboxServiceTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailboxServiceTests.class);

    @Inject
    MailboxService service;

    @Inject
    BundleContext bundleContext;

    /**
     * <p>
     * Exercises a 'mailbox' service provided by an OSGi bundle that depends on the driver.  Ensures that
     * queries can be made through the service with the current given configuration.
     * </p>
     * <p/>
     * The following configurations are tried (defined via methods with the @Configuration annotation):
     * <ol>
     * <li>Default bundle (Driver with all of it's dependencies and Guava 16.0.1)</li>
     * <li>Shaded bundle (Driver with netty shaded and Guava 16.0.1)</li>
     * <li>With Guava 17</li>
     * <li>With Guava 18</li>
     * <li>With Guava 19</li>
     * </ol>
     */
    protected void checkService() throws MailboxException {
        if (LOGGER.isDebugEnabled()) {
            for (Bundle bundle : bundleContext.getBundles()) {
                LOGGER.debug("Loaded bundle: {} {}", bundle.getSymbolicName(), bundle.getVersion());
            }
        }
        // Insert some data into mailbox for a particular user.
        String recipient = "user@datastax.com";
        try {
            Collection<MailboxMessage> inMessages = new ArrayList<MailboxMessage>();
            for (int i = 0; i < 30; i++) {
                MailboxMessage message = new MailboxMessage(recipient, new GregorianCalendar(2015, 1, i).getTimeInMillis(), recipient, "" + i);
                inMessages.add(message);
                service.sendMessage(message);
            }

            Iterable<MailboxMessage> messages = service.getMessages(recipient);

            assertEquals(messages, inMessages);
        } finally {
            service.clearMailbox(recipient);
        }
    }
}
