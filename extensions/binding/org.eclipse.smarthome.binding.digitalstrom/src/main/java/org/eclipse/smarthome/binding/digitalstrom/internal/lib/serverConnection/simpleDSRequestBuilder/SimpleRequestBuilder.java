/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.simpleDSRequestBuilder;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.NullArgumentException;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.simpleDSRequestBuilder.constants.ExeptionConstants;

/**
 * The {@link SimpleRequestBuilder} build a request string.<br>
 * <br>
 * <i><b>Code example</b><br>
 * String requestString = {@link SimpleRequestBuilder}.{@link #buildNewRequest(String)}.<br>
 * <span style="padding-left:14em">{@link #addRequestClass(String)}.<br>
 * </span>
 * <span style="padding-left:14em">{@link #addFunction(String)}.<br>
 * </span>
 * <span style="padding-left:14em">{@link #addParameter(String, String)}. (optional)<br>
 * </span>
 * <span style="padding-left:14em">{@link #addParameter(String, String)}. (optional)<br>
 * </span>
 * <span style="padding-left:14em">{@link #buildRequestString()};<br>
 * </span></i>
 *
 * @author Michael Ochel - initial contributer
 * @author Matthias Siegele - initial contributer
 */
public class SimpleRequestBuilder {

    // states
    private boolean functionIsChosen = false;
    private boolean parameterIsAdded = false;
    private boolean classIsChosen = false;

    private String request = null;
    private static SimpleRequestBuilder builder = null;
    private static final Lock lock = new ReentrantLock();

    private SimpleRequestBuilder() {

    }

    /**
     * Returns a {@link SimpleRequestBuilder} with the given intefaceKey as chosen request-interface.
     *
     * @param interfaceKey must not be null
     * @return simpleRequestBuilder with chosen interface
     * @throws NullArgumentException if the interfaceKey is null
     */
    public static SimpleRequestBuilder buildNewRequest(String interfaceKey) throws NullArgumentException {
        if (builder == null) {
            builder = new SimpleRequestBuilder();
        }
        lock.lock();
        return builder.buildNewRequestInt(interfaceKey);
    }

    private SimpleRequestBuilder buildNewRequestInt(String interfaceKey) {
        if (interfaceKey == null) {
            throw new NullArgumentException("interfaceKey");
        }
        request = "/" + interfaceKey + "/";
        classIsChosen = false;
        functionIsChosen = false;
        parameterIsAdded = false;
        return this;
    }

    /**
     * Adds a requestClass to the request-string.
     *
     * @param requestClassKey must not be null
     * @return simpleRequestBuilder with chosen requestClass
     * @throws IllegalArgumentException if a requestClass is already chosen
     * @throws NullArgumentException if the requestClassKey is null
     */
    public SimpleRequestBuilder addRequestClass(String requestClassKey)
            throws IllegalArgumentException, NullArgumentException {
        return builder.addRequestClassInt(requestClassKey);
    }

    private SimpleRequestBuilder addRequestClassInt(String requestClassKey) {
        if (!classIsChosen && requestClassKey != null) {
            classIsChosen = true;
            request = request + requestClassKey + "/";
        } else {
            if (!classIsChosen) {
                throw new IllegalArgumentException(ExeptionConstants.CLASS_ALREADY_ADDED);
            } else {
                throw new NullArgumentException("requestClassKey");
            }
        }
        return this;
    }

    /**
     * Adds a function to the request-string.
     *
     * @param functionKey must not be null
     * @return SimpleRequestBuilder with chosen function
     * @throws IllegalArgumentException if a function is already chosen
     * @throws NullArgumentException if the functionKey is null
     */
    public SimpleRequestBuilder addFunction(String functionKey) throws IllegalArgumentException, NullArgumentException {
        return builder.addFunctionInt(functionKey);
    }

    private SimpleRequestBuilder addFunctionInt(String functionKey) {
        if (!classIsChosen) {
            throw new IllegalArgumentException(ExeptionConstants.NO_CLASS_ADDED);
        }
        if (!functionIsChosen) {
            if (functionKey != null) {
                functionIsChosen = true;
                request = request + functionKey;
            } else {
                throw new NullArgumentException("functionKey");
            }
        } else {
            throw new IllegalArgumentException(ExeptionConstants.FUNCTION_ALLREADY_ADDED);
        }
        return this;
    }

    /**
     * Adds a parameter to the request-string, if the parameter value is not null.
     *
     * @param parameterKey must not be null
     * @param parameterValue can be null
     * @return SimpleRequestBuilder with added parameter
     * @throws IllegalArgumentException if no class and function added
     * @throws NullArgumentException if the parameterKey is null
     */
    public SimpleRequestBuilder addParameter(String parameterKey, String parameterValue)
            throws IllegalArgumentException, NullArgumentException {
        return builder.addParameterInt(parameterKey, parameterValue);
    }

    private SimpleRequestBuilder addParameterInt(String parameterKey, String parameterValue) {
        if (allRight()) {
            if (parameterKey == null) {
                throw new NullArgumentException("parameterKey");
            }
            if (parameterValue != null) {
                if (!parameterIsAdded) {
                    parameterIsAdded = true;
                    request = request + "?" + parameterKey + "=" + parameterValue;
                } else {
                    request = request + "&" + parameterKey + "=" + parameterValue;
                }
            }
        }
        return this;
    }

    /**
     * Returns the request string.
     *
     * @return request string
     * @throws IllegalArgumentException if no class or function is added.
     */
    public String buildRequestString() throws IllegalArgumentException {
        String request = builder.buildRequestStringInt();
        lock.unlock();
        return request;
    }

    private String buildRequestStringInt() {
        return allRight() ? request : null;
    }

    private boolean allRight() {
        if (!classIsChosen) {
            throw new IllegalArgumentException(ExeptionConstants.NO_CLASS_ADDED);
        }
        if (!functionIsChosen) {
            throw new IllegalArgumentException(ExeptionConstants.NO_FUNCTION);
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals()
     */
    public boolean equals(SimpleRequestBuilder builder) {
        return this.request.contains(builder.request);
    }
}
