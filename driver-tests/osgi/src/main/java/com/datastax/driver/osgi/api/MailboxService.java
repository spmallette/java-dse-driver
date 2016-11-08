/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.osgi.api;

public interface MailboxService {

    /**
     * Retrieve all messages for a given recipient.
     *
     * @param recipient User whose mailbox is being read.
     * @return All messages in the mailbox.
     */
    public Iterable<MailboxMessage> getMessages(String recipient) throws MailboxException;

    /**
     * Stores the given message in the appropriate mailbox.
     *
     * @param message Message to send.
     * @return The timestamp generated for the message (milliseconds since the Epoch).
     */
    public long sendMessage(MailboxMessage message) throws MailboxException;

    /**
     * Deletes all mail for the given recipient.
     *
     * @param recipient User whose mailbox will be cleared.
     */
    public void clearMailbox(String recipient) throws MailboxException;
}
