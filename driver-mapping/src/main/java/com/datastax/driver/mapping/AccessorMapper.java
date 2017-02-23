/*
 *      Copyright (C) 2012-2017 DataStax Inc.
 *
 *      This software can be used solely with DataStax Enterprise. Please consult the license at
 *      http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.driver.mapping;

import com.datastax.driver.core.PreparedStatement;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

class AccessorMapper<T> {

    final Class<T> daoClass;
    final List<MethodMapper> methods;
    private final Class<T>[] proxyClasses;
    private final AccessorInvocationHandler<T> handler;

    @SuppressWarnings({"unchecked", "rawtypes"})
    AccessorMapper(Class<T> daoClass, List<MethodMapper> methods) {
        this.daoClass = daoClass;
        this.methods = methods;
        this.proxyClasses = (Class<T>[]) new Class[]{daoClass};
        this.handler = new AccessorInvocationHandler<T>(this);
    }

    @SuppressWarnings("unchecked")
    T createProxy() {
        try {
            return (T) Proxy.newProxyInstance(daoClass.getClassLoader(), proxyClasses, handler);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create instance for Accessor interface " + daoClass.getName());
        }
    }

    void prepare(MappingManager manager) {
        List<ListenableFuture<PreparedStatement>> statements = new ArrayList<ListenableFuture<PreparedStatement>>(methods.size());

        for (MethodMapper method : methods)
            statements.add(manager.getSession().prepareAsync(method.queryString));

        try {
            List<PreparedStatement> preparedStatements = Futures.allAsList(statements).get();
            for (int i = 0; i < methods.size(); i++)
                methods.get(i).prepare(manager, preparedStatements.get(i));
        } catch (Exception e) {
            throw new RuntimeException("Error preparing queries for accessor " + daoClass.getSimpleName(), e);
        }
    }

}
