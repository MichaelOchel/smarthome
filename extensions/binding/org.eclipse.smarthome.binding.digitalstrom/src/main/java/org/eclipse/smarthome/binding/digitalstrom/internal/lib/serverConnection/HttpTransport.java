/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection;

/**
 * The {@link HttpTransport} executes an request to the DigitalSTROM-Server.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public interface HttpTransport {

    /**
     * Executes a digitalSTROM-request through calling {@link #execute(String, int, int)} with default connection time
     * out and read timeout.
     *
     * @param request to execute
     * @return response
     */
    public String execute(String request);

    /**
     * Executes a digitalSTROM-request.
     *
     * @param request to execute
     * @param connectTimeout of execution
     * @param readTimeout of execution
     * @return response
     */
    public String execute(String request, int connectTimeout, int readTimeout);

    /**
     * Executes a digitalSTROM test request and returns the HTTP-Code.
     *
     * @param testRequest to execute
     * @return HTTP-Code
     */
    public int checkConnection(String testRequest);

    /**
     * Returns the connection timeout for sensor data readings.
     *
     * @return sensor data connection timeout
     */
    public int getSensordataConnectionTimeout();

    /**
     * Returns the read timeout for sensor data readings.
     *
     * @return sensor data read timeout
     */
    public int getSensordataReadTimeout();

    /**
     * Saves the SSL-Certificate in a file at the given path.
     *
     * @param path to save
     * @return absolute path
     */
    public String writePEMCertFile(String path);
}