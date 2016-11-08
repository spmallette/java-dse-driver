/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.osgi.api;

public class MailboxException extends Exception {

    public MailboxException(Throwable cause) {
        super("Failure interacting with Mailbox", cause);
    }
}
