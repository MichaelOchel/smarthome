/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMServerConnection;
/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

/**
 * The {@link HttpTransport} executes an request to the DigitalSTROM-Server.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public interface HttpTransport {

    /**
     * Executes a digitalSTROM-request.
     *
     * @param request
     * @return response
     */
    public String execute(String request);

    /**
     * Executes a digitalSTROM-request.
     *
     * @param request
     * @param connectTimeout
     * @param readTimeout
     * @return response
     */
    public String execute(String request, int connectTimeout, int readTimeout);

    /**
     * Executes a digitalSTROM test request and returns the HTTP-Code.
     *
     * @param testRequest
     * @return HTTP-Code
     */
    public int checkConnection(String testRequest);

}