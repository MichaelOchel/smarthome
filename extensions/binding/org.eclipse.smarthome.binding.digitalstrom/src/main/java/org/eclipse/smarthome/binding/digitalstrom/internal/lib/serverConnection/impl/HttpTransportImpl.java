/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.config.Config;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.HttpTransport;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.simpleDSRequestBuilder.constants.ParameterKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HttpTransportImpl} executes an request to the digitalSTROM-Server.
 * <p>
 * If a {@link Config} is given at the constructor. It sets the SSL-Certificate what is set in
 * {@link Config#getCert()}. If there is no SSL-Certificate, but an path to an external SSL-Certificate file what is set
 * in {@link Config#getTrustCertPath()} this will be set. If no SSL-Certificate is set in the {@link Config} it will be
 * red out from the server and set in {@link Config#setCert(String)}.
 * </p>
 * <p>
 * If no {@link Config} is given the SSL-Certificate will be stored locally.
 * </p>
 * <p>
 * The method {@link #writePEMCertFile(String)} saves the SSL-Certificate in a file at the given path. If all
 * SSL-Certificates shout be ignored the flag <i>exeptAllCerts</i> have to be true at the constructor
 * </p>
 * <p>
 * If a {@link ConnectionManager} is given at the constructor, the session-token is not needed by requests and the
 * {@link ConnectionListener}, which is registered at the {@link ConnectionManager}, will be automatically informed
 * about
 * connection state changes through the {@link #execute(String, int, int)} method.
 * </p>
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class HttpTransportImpl implements HttpTransport {

    private final static String LINE_SEPERATOR = System.getProperty("line.separator");
    private final static String BEGIN_CERT = "-----BEGIN CERTIFICATE-----" + LINE_SEPERATOR;
    private final static String END_CERT = LINE_SEPERATOR + "-----END CERTIFICATE-----" + LINE_SEPERATOR;

    private static final Logger logger = LoggerFactory.getLogger(HttpTransportImpl.class);

    private String uri;

    private int connectTimeout;
    private int readTimeout;

    // private long lastRequest = 0;

    private Config config = null;

    private ConnectionManager connectionManager = null;

    private String cert = null;
    private SSLSocketFactory sslSocketFactory = null;
    private HostnameVerifier hostnameVerifier = new HostnameVerifier() {

        @Override
        public boolean verify(String arg0, SSLSession arg1) {
            return arg0.equals(arg1.getPeerHost()) || arg0.contains("dss.local.");
        }
    };

    /**
     * Creates a new {@link HttpTransportImpl} with registration of the given {@link ConnectionManager} and set ignore
     * all SSL-Certificates. The {@link Config} will be automatically added from the configurations of the given
     * {@link ConnectionManager}.
     *
     * @param connectionManager
     * @param exeptAllCerts (true = all will ignore)
     */
    public HttpTransportImpl(ConnectionManager connectionManager, boolean exeptAllCerts) {
        this.connectionManager = connectionManager;
        this.config = connectionManager.getConfig();
        init(config.getHost(), config.getConnectionTimeout(), config.getReadTimeout(), exeptAllCerts);
    }

    /**
     * Creates a new {@link HttpTransportImpl} with configurations of the given {@link Config} and set ignore all
     * SSL-Certificates.
     *
     * @param config
     * @param exeptAllCerts (true = all will ignore)
     */
    public HttpTransportImpl(Config config, boolean exeptAllCerts) {
        this.config = config;
        init(config.getHost(), config.getConnectionTimeout(), config.getReadTimeout(), exeptAllCerts);
    }

    /**
     * Creates a new {@link HttpTransportImpl} with configurations of the given {@link Config}.
     *
     * @param config
     */
    public HttpTransportImpl(Config config) {
        this.config = config;
        init(config.getHost(), config.getConnectionTimeout(), config.getReadTimeout(), false);
    }

    /**
     * Creates a new {@link HttpTransportImpl}.
     *
     * @param uri
     */
    public HttpTransportImpl(String uri) {
        init(uri, Config.DEFAULT_CONNECTION_TIMEOUT, Config.DEFAULT_READ_TIMEOUT, false);
    }

    /**
     * Creates a new {@link HttpTransportImpl} and set ignore all SSL-Certificates.
     *
     * @param uri
     * @param exeptAllCerts (true = all will ignore)
     */
    public HttpTransportImpl(String uri, boolean exeptAllCerts) {
        init(uri, Config.DEFAULT_CONNECTION_TIMEOUT, Config.DEFAULT_READ_TIMEOUT, exeptAllCerts);
    }

    /**
     * Creates a new {@link HttpTransportImpl}.
     *
     * @param uri
     * @param connectTimeout
     * @param readTimeout
     */
    public HttpTransportImpl(String uri, int connectTimeout, int readTimeout) {
        init(uri, connectTimeout, readTimeout, false);
    }

    /**
     * Creates a new {@link HttpTransportImpl} and set ignore all SSL-Certificates..
     *
     * @param uri
     * @param connectTimeout
     * @param readTimeout
     * @param exeptAllCerts (true = all will ignore)
     */
    public HttpTransportImpl(String uri, int connectTimeout, int readTimeout, boolean exeptAllCerts) {
        init(uri, connectTimeout, readTimeout, exeptAllCerts);
    }

    private void init(String uri, int connectTimeout, int readTimeout, boolean exeptAllCerts) {
        this.uri = fixURI(uri);
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        // Check SSL Certificate
        if (exeptAllCerts) {
            sslSocketFactory = generateSSLContextWhichAcceptAllSSLCertificats();
        } else {
            if (config != null) {
                cert = config.getCert();
                logger.debug("generate SSLcontext from config cert");
                if (StringUtils.isNotBlank(cert)) {
                    sslSocketFactory = generateSSLContextFromPEMCertString(cert);
                } else {
                    if (StringUtils.isNotBlank(config.getTrustCertPath())) {
                        logger.debug("generate SSLcontext from config cert path");
                        cert = readPEMCertificateStringFromFile(config.getTrustCertPath());
                        if (StringUtils.isNotBlank(cert)) {
                            sslSocketFactory = generateSSLContextFromPEMCertString(cert);
                        }
                    } else {
                        logger.debug("generate SSLcontext from server");
                        cert = getPEMCertificateFromServer(this.uri);
                        sslSocketFactory = generateSSLContextFromPEMCertString(cert);
                        if (sslSocketFactory != null) {
                            config.setCert(cert);
                        }
                    }
                }
            } else {
                logger.debug("generate SSLcontext from server");
                cert = getPEMCertificateFromServer(this.uri);
                sslSocketFactory = generateSSLContextFromPEMCertString(cert);
            }
        }
    }

    private String fixURI(String uri) {
        if (!uri.startsWith("https://")) {
            uri = "https://" + uri;
        }
        if (uri.split(":").length != 3) {
            uri = uri + ":8080";
        }
        return uri;
    }

    private String fixRequest(String request) {
        return request.replace(" ", "");
    }

    @Override
    public String execute(String request) {
        return execute(request, this.connectTimeout, this.readTimeout);
    }

    private short loginCounter = 0;

    @Override
    public String execute(String request, int connectTimeout, int readTimeout) {
        // NOTE: We will only show exceptions in the debug level, because they will be handled in the checkConnection()
        // method and this changes the bridge state. If a command was send it fails than and a sensorJob will be
        // execute the next time, by TimeOutExceptions. By other exceptions the checkConnection() method handles it in
        // max 1 second.
        String response = null;
        HttpsURLConnection connection = null;
        try {
            request = checkSessionToken(request);
            connection = getConnection(request, connectTimeout, readTimeout);
            if (connection != null) {
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    response = IOUtils.toString(connection.getInputStream());
                    if (!response.contains("Authentication failed")) {
                        if (loginCounter > 0) {
                            connectionManager.checkConnection(connection.getResponseCode());
                        }
                        loginCounter = 0;
                    } else {
                        connectionManager.checkConnection(-6);
                        loginCounter++;
                    }
                }
                connection.disconnect();
                if (response == null && connectionManager != null && loginCounter < 2) {
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                        execute(addSessionToken(request, connectionManager.getNewSessionToken()), connectTimeout,
                                readTimeout);
                        loginCounter++;
                    } else {
                        connectionManager.checkConnection(connection.getResponseCode());
                        loginCounter++;
                        return null;
                    }
                }
                return response;
            }
        } catch (SocketTimeoutException e) {
            informConnectionManager(-4);
        } catch (java.net.ConnectException e) {
            informConnectionManager(-3);
        } catch (MalformedURLException e) {
            informConnectionManager(-2);
        } catch (IOException e) {
            if (e instanceof java.net.ConnectException) {
                informConnectionManager(-3);
            } else if (e instanceof java.net.UnknownHostException) {
                informConnectionManager(-5);
            } else if (connectionManager != null) {
                logger.error("An IOException occurred by executing jsonRequest: " + request, e);
                informConnectionManager(-1);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    private void informConnectionManager(int code) {
        if (connectionManager != null && loginCounter < 2) {
            connectionManager.checkConnection(code);
        }
    }

    private String checkSessionToken(String request) {
        if (checkNeededSessionToken(request)) {
            if (connectionManager != null) {
                String sessionToken = connectionManager.getSessionToken();
                if (sessionToken == null) {
                    return addSessionToken(request, connectionManager.getNewSessionToken());
                }
                request = addSessionToken(request, sessionToken);
            }
        }
        return request;
    }

    private boolean checkNeededSessionToken(String request) {
        String functionName = StringUtils.substringAfterLast(StringUtils.substringBefore(request, "?"), "/");
        return !DsAPIImpl.METHODS_MUST_NOT_BE_LOGGED_IN.contains(functionName);
    }

    private String addSessionToken(String request, String sessionToken) {
        if (!request.contains(ParameterKeys.TOKEN)) {
            if (request.contains("?")) {
                request = request + "&" + ParameterKeys.TOKEN + "=" + sessionToken;
            } else {
                request = request + "?" + ParameterKeys.TOKEN + "=" + sessionToken;
            }
        } else {
            /*
             * int start = request.indexOf("token=");
             * int end = request.indexOf("&", start);
             * if (end == -1) {
             * request = request.substring(0, start + 6) + sessionToken;
             * } else {
             * request = request.substring(0, start + 6) + sessionToken + request.substring(end, request.length());
             * }
             */
            request = StringUtils.replaceOnce(request,
                    StringUtils.substringBefore(StringUtils.substringAfter(request, ParameterKeys.TOKEN + "="), "&"),
                    sessionToken);

        }
        return request;
    }

    private HttpsURLConnection getConnection(String request, int connectTimeout, int readTimeout) throws IOException {
        if (StringUtils.isNotBlank(request)) {
            request = fixRequest(request);
            URL url = new URL(this.uri + request);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            if (connection != null) {
                connection.setConnectTimeout(connectTimeout);
                connection.setReadTimeout(readTimeout);
                if (sslSocketFactory != null) {
                    connection.setSSLSocketFactory(sslSocketFactory);
                }
                if (hostnameVerifier != null) {
                    connection.setHostnameVerifier(hostnameVerifier);
                }
            }
            return connection;
        }
        return null;
    }

    @Override
    public int checkConnection(String testRequest) {
        try {
            HttpsURLConnection connection = getConnection(testRequest, connectTimeout, readTimeout);
            if (connection != null) {
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    if (IOUtils.toString(connection.getInputStream()).contains("Authentication failed")) {
                        return -6;
                    }
                }
                connection.disconnect();
                return connection.getResponseCode();
            }
        } catch (SocketTimeoutException e) {
            return -4;
        } catch (java.net.ConnectException e) {
            return -3;
        } catch (MalformedURLException e) {
            return -2;
        } catch (IOException e) {
            if (e instanceof java.net.ConnectException) {
                return -3;
            }
            if (e instanceof java.net.UnknownHostException) {
                return -5;
            }
            logger.error("An IOException occurred by executing jsonRequest: " + testRequest, e);
        }
        return -1;
    }

    @Override
    public int getSensordataConnectionTimeout() {
        return config != null ? config.getSensordataConnectionTimeout() : Config.DEFAULT_SENSORDATA_CONNECTION_TIMEOUT;
    }

    @Override
    public int getSensordataReadTimeout() {
        return config != null ? config.getSensordataReadTimeout() : Config.DEFAULT_SENSORDATA_READ_TIMEOUT;
    }

    private String readPEMCertificateStringFromFile(String path) {
        if (StringUtils.isBlank(path)) {
            logger.error("Path is empty.");
        } else {
            File dssCert = new File(path);
            if (dssCert.exists()) {
                if (path.endsWith(".crt")) {
                    try {
                        InputStream certInputStream = new FileInputStream(dssCert);
                        String cert = IOUtils.toString(certInputStream);
                        if (cert.startsWith(BEGIN_CERT)) {
                            return cert;
                        } else {
                            logger.error(
                                    "File is not a PEM certificate file. PEM-Certificats starts with: " + BEGIN_CERT);
                        }
                    } catch (FileNotFoundException e) {
                        logger.error("Can't find a certificate file at the path: " + path + "\nPlease check the path!");
                    } catch (IOException e) {
                        logger.error("An IOException occurred: ", e);
                    }
                } else {
                    logger.error("File is not a certificate (.crt) file.");
                }
            } else {
                logger.error("File not found");
            }
        }
        return null;
    }

    @Override
    public String writePEMCertFile(String path) {
        path = StringUtils.trimToEmpty(path);
        File certFilePath;
        if (StringUtils.isNotBlank(path)) {
            certFilePath = new File(path);
            boolean pathExists = certFilePath.exists();
            if (!pathExists) {
                pathExists = certFilePath.mkdirs();
            }
            if (pathExists && !path.endsWith("/")) {
                path = path + "/";
            }
        }
        InputStream certInputStream = IOUtils.toInputStream(cert);
        X509Certificate trustedCert;
        try {
            trustedCert = (X509Certificate) CertificateFactory.getInstance("X.509")
                    .generateCertificate(certInputStream);

            certFilePath = new File(path + trustedCert.getSubjectDN().getName().split(",")[0].substring(2) + ".crt");
            if (!certFilePath.exists()) {
                certFilePath.createNewFile();
                FileWriter writer = new FileWriter(certFilePath, true);
                writer.write(cert);
                writer.flush();
                writer.close();
                return certFilePath.getAbsolutePath();
            } else {
                logger.error("File allready exists!");
            }
        } catch (IOException e) {
            logger.error("An IOException occurred: ", e);
        } catch (CertificateException e1) {
            logger.error("A CertificateException occurred: ", e1);
        }
        return null;
    }

    private SSLSocketFactory generateSSLContextFromPEMCertString(String pemCert) {
        if (StringUtils.isNotBlank(pemCert) && pemCert.startsWith(BEGIN_CERT)) {
            try {
                InputStream certInputStream = IOUtils.toInputStream(pemCert);
                final X509Certificate trustedCert = (X509Certificate) CertificateFactory.getInstance("X.509")
                        .generateCertificate(certInputStream);

                final TrustManager[] trustManager = new TrustManager[] { new X509TrustManager() {

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
                            throws CertificateException {
                        if (!certs[0].equals(trustedCert)) {
                            throw new CertificateException();
                        }
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
                            throws CertificateException {
                        if (!certs[0].equals(trustedCert)) {
                            throw new CertificateException();
                        }
                    }
                } };

                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustManager, new java.security.SecureRandom());
                return sslContext.getSocketFactory();
            } catch (NoSuchAlgorithmException e) {
                logger.error("A NoSuchAlgorithmException occurred: ", e);
            } catch (KeyManagementException e) {
                logger.error("A KeyManagementException occurred: ", e);
            } catch (CertificateException e) {
                logger.error("A CertificateException occurred: ", e);
            }
        } else {
            logger.error("Cert is empty");
        }
        return null;
    }

    private String getPEMCertificateFromServer(String host) {
        try {
            URL url = new URL(host);

            HttpsURLConnection connection = null;

            connection = (HttpsURLConnection) url.openConnection();
            connection.setHostnameVerifier(hostnameVerifier);
            connection.setSSLSocketFactory(generateSSLContextWhichAcceptAllSSLCertificats());
            connection.connect();

            java.security.cert.Certificate[] cert = connection.getServerCertificates();
            connection.disconnect();

            byte[] by = ((X509Certificate) cert[0]).getEncoded();
            if (by.length != 0) {
                return BEGIN_CERT + DatatypeConverter.printBase64Binary(by) + END_CERT;
            }
        } catch (MalformedURLException e) {
            logger.error("A MalformedURLException occurred: ", e);
        } catch (IOException e) {
            logger.error("An IOException occurred: ", e);
        } catch (CertificateEncodingException e) {
            logger.error("A CertificateEncodingException occurred: ", e);
        }
        return null;
    }

    private SSLSocketFactory generateSSLContextWhichAcceptAllSSLCertificats() {
        Security.addProvider(Security.getProvider("SunJCE"));
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

            }
        } };

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");

            sslContext.init(null, trustAllCerts, new SecureRandom());

            return sslContext.getSocketFactory();
        } catch (KeyManagementException e) {
            logger.error("A KeyManagementException occurred", e);
        } catch (NoSuchAlgorithmException e) {
            logger.error("A NoSuchAlgorithmException occurred", e);
        }
        return null;
    }
}