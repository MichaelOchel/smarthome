/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.simpleURLBuilder;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.simpleURLBuilder.constants.ExeptionConstants;

public class SimpleRequestBuilder {

    // states
    private boolean functionIsAdded = false;
    private boolean parameterIsAdded = false;
    private boolean classIsChose = false;
    private boolean interfaceIsChose;

    private String request = null;
    private static SimpleRequestBuilder builder = null;
    private static final Lock lock = new ReentrantLock();

    /**
     *
     */
    private SimpleRequestBuilder() throws Exception {

    }

    public static SimpleRequestBuilder buildNewRequest(String interface_) throws Exception {
        if (builder == null) {
            builder = new SimpleRequestBuilder();
        }
        lock.lock();
        return builder.buildNewRequestInt(interface_);
    }

    private SimpleRequestBuilder buildNewRequestInt(String interface_) throws Exception {
        if (!interfaceIsChose) {
            interfaceIsChose = true;
        }
        request = "/" + interface_ + "/";
        classIsChose = false;
        functionIsAdded = false;
        parameterIsAdded = false;
        return this;
    }

    public SimpleRequestBuilder addRequestClass(String requestClass) throws Exception {
        return builder.addRequestClassInt(requestClass);
    }

    private SimpleRequestBuilder addRequestClassInt(String requestClass) throws Exception {
        if (!classIsChose) {
            classIsChose = true;
        }
        request = request + requestClass + "/";
        functionIsAdded = false;
        parameterIsAdded = false;
        return this;
    }

    public SimpleRequestBuilder addFunction(String function) throws Exception {
        return builder.addFunctionInt(function);
    }

    private SimpleRequestBuilder addFunctionInt(String function) throws Exception {
        if (!classIsChose) {
            throw new Exception(ExeptionConstants.NO_CLASS_ADDED);
        }
        if (!functionIsAdded) {
            if (function != null) {
                functionIsAdded = true;
                request = request + function;
            } else {
                throw new NullPointerException(ExeptionConstants.NULL_FUNCTION);
            }
        } else {
            throw new Exception(ExeptionConstants.FUNCTION_ALLREADY_ADDED);
        }
        return this;
    }

    public SimpleRequestBuilder addParameter(String parameterType, String value) throws Exception {
        return builder.addParameterInt(parameterType, value);
    }

    private SimpleRequestBuilder addParameterInt(String parameterType, String value) throws Exception {
        if (allRight()) {
            if (value != null) {
                if (!parameterIsAdded) {
                    parameterIsAdded = true;
                    request = request + "?" + parameterType + "=" + value;
                } else {
                    request = request + "&" + parameterType + "=" + value;
                }
            }
        } else {
            throw new Exception(ExeptionConstants.NO_FUNCTION);
        }
        return this;
    }

    public String buildRequestString() throws Exception {
        String request = builder.buildRequestStringInt();
        lock.unlock();
        return request;
    }

    private String buildRequestStringInt() throws Exception {
        return allRight() ? request : null;
    }

    private boolean allRight() throws Exception {
        if (!interfaceIsChose) {
            throw new Exception(ExeptionConstants.NO_INTERFACE_ADDED);
        }
        if (!classIsChose) {
            throw new Exception(ExeptionConstants.NO_CLASS_ADDED);
        }
        if (!functionIsAdded) {
            throw new Exception(ExeptionConstants.NO_FUNCTION);
        }
        return true;
    }

    public boolean equals(SimpleRequestBuilder builder) {
        return this.request.contains(builder.request);
    }
}
