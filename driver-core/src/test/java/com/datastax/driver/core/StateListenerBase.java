/*
 * Copyright (C) 2012-2017 DataStax Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.core;

public class StateListenerBase implements Host.StateListener {
    @Override
    public void onAdd(Host host) {
    }

    @Override
    public void onUp(Host host) {
    }

    @Override
    public void onDown(Host host) {
    }

    @Override
    public void onRemove(Host host) {
    }

    @Override
    public void onRegister(Cluster cluster) {

    }

    @Override
    public void onUnregister(Cluster cluster) {

    }

}
