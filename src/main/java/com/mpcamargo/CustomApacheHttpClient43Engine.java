//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.mpcamargo;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.apache.commons.io.output.DeferredFileOutputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClientEngine;
import org.jboss.resteasy.client.jaxrs.engines.HttpContextProvider;
import org.jboss.resteasy.client.jaxrs.engines.SelfExpandingBufferredInputStream;
import org.jboss.resteasy.client.jaxrs.i18n.LogMessages;
import org.jboss.resteasy.client.jaxrs.i18n.Messages;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;
import org.jboss.resteasy.client.jaxrs.internal.FinalizedClientResponse;
import org.jboss.resteasy.spi.config.ConfigurationFactory;
import org.jboss.resteasy.util.CaseInsensitiveMap;

public class CustomApacheHttpClient43Engine implements ApacheHttpClientEngine {
    static final String FILE_UPLOAD_IN_MEMORY_THRESHOLD_PROPERTY = "org.jboss.resteasy.client.jaxrs.engines.fileUploadInMemoryThreshold";
    private static final String processId;
    protected final HttpClient httpClient;
    protected boolean closed;
    protected final boolean allowClosingHttpClient;
    protected HttpContextProvider httpContextProvider;
    protected SSLContext sslContext;
    protected HostnameVerifier hostnameVerifier;
    protected int responseBufferSize;
    protected HttpHost defaultProxy;
    protected boolean chunked;
    protected boolean followRedirects;
    protected int fileUploadInMemoryThresholdLimit;
    protected ApacheHttpClientEngine.MemoryUnit fileUploadMemoryUnit;
    protected File fileUploadTempFileDir;

    public CustomApacheHttpClient43Engine() {
        this((HttpClient)null, (HttpContextProvider)null, true, (HttpHost)null);
    }

    public CustomApacheHttpClient43Engine(HttpHost defaultProxy) {
        this((HttpClient)null, (HttpContextProvider)null, true, defaultProxy);
    }

    public CustomApacheHttpClient43Engine(HttpClient httpClient) {
        this(httpClient, (HttpContextProvider)null, true, (HttpHost)null);
    }

    public CustomApacheHttpClient43Engine(HttpClient httpClient, boolean closeHttpClient) {
        this(httpClient, (HttpContextProvider)null, closeHttpClient, (HttpHost)null);
    }

    public CustomApacheHttpClient43Engine(HttpClient httpClient, HttpContextProvider httpContextProvider) {
        this(httpClient, httpContextProvider, true, (HttpHost)null);
    }

    private CustomApacheHttpClient43Engine(HttpClient httpClient, HttpContextProvider httpContextProvider, boolean closeHttpClient, HttpHost defaultProxy) {
        this.responseBufferSize = 8192;
        this.defaultProxy = null;
        this.chunked = false;
        this.followRedirects = false;
        this.fileUploadInMemoryThresholdLimit = 1;
        this.fileUploadMemoryUnit = MemoryUnit.MB;
        this.fileUploadTempFileDir = getTempDir();
        this.httpClient = httpClient != null ? httpClient : this.createDefaultHttpClient();
        if (closeHttpClient && !(this.httpClient instanceof CloseableHttpClient)) {
            throw new IllegalArgumentException("httpClient must be a CloseableHttpClient instance in order for allowing engine to close it!");
        } else {
            this.httpContextProvider = httpContextProvider;
            this.allowClosingHttpClient = closeHttpClient;
            this.defaultProxy = defaultProxy;

            try {
                int threshold = Integer.parseInt((String)ConfigurationFactory.getInstance().getConfiguration().getOptionalValue("org.jboss.resteasy.client.jaxrs.engines.fileUploadInMemoryThreshold", String.class).orElse("1"));
                if (threshold > -1) {
                    this.fileUploadInMemoryThresholdLimit = threshold;
                }

                LogMessages.LOGGER.debugf("Negative threshold, %s, specified. Using default value", threshold);
            } catch (Exception var6) {
                LogMessages.LOGGER.debug("Exception caught parsing memory threshold. Using default value.", var6);
            }

        }
    }

    public int getResponseBufferSize() {
        return this.responseBufferSize;
    }

    public void setResponseBufferSize(int responseBufferSize) {
        this.responseBufferSize = responseBufferSize;
    }

    public int getFileUploadInMemoryThresholdLimit() {
        return this.fileUploadInMemoryThresholdLimit;
    }

    public void setFileUploadInMemoryThresholdLimit(int fileUploadInMemoryThresholdLimit) {
        this.fileUploadInMemoryThresholdLimit = fileUploadInMemoryThresholdLimit;
    }

    public ApacheHttpClientEngine.MemoryUnit getFileUploadMemoryUnit() {
        return this.fileUploadMemoryUnit;
    }

    public void setFileUploadMemoryUnit(ApacheHttpClientEngine.MemoryUnit fileUploadMemoryUnit) {
        this.fileUploadMemoryUnit = fileUploadMemoryUnit;
    }

    public File getFileUploadTempFileDir() {
        return this.fileUploadTempFileDir;
    }

    public void setFileUploadTempFileDir(File fileUploadTempFileDir) {
        this.fileUploadTempFileDir = fileUploadTempFileDir;
    }

    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    public SSLContext getSslContext() {
        return this.sslContext;
    }

    public void setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    public HostnameVerifier getHostnameVerifier() {
        return this.hostnameVerifier;
    }

    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
    }

    public static CaseInsensitiveMap<String> extractHeaders(HttpResponse response) {
        CaseInsensitiveMap<String> headers = new CaseInsensitiveMap();
        Header[] var2 = response.getAllHeaders();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Header header = var2[var4];
            headers.add(header.getName(), header.getValue());
        }

        return headers;
    }

    protected InputStream createBufferedStream(InputStream is) {
        if (this.responseBufferSize == 0) {
            return is;
        } else {
            return (InputStream)(this.responseBufferSize < 0 ? new SelfExpandingBufferredInputStream(is) : new BufferedInputStream(is, this.responseBufferSize));
        }
    }

    public Response invoke(Invocation inv) {
        ClientInvocation request = (ClientInvocation)inv;
        String uri = request.getUri().toString();
        final HttpRequestBase httpMethod = this.createHttpMethod(uri, request.getMethod());

        final HttpResponse res;
        try {
            this.loadHttpMethod(request, httpMethod);
            if (System.getSecurityManager() == null) {
                res = this.httpClient.execute(httpMethod, this.httpContextProvider == null ? null : this.httpContextProvider.getContext());
            } else {
                try {
                    res = AccessController.doPrivileged(new PrivilegedExceptionAction<HttpResponse>() {
                        public HttpResponse run() throws Exception {
                            return CustomApacheHttpClient43Engine.this.httpClient.execute(httpMethod, CustomApacheHttpClient43Engine.this.httpContextProvider == null ? null : CustomApacheHttpClient43Engine.this.httpContextProvider.getContext());
                        }
                    });
                } catch (PrivilegedActionException var11) {
                    throw new RuntimeException(var11);
                }
            }
        } catch (Exception var12) {
            LogMessages.LOGGER.clientSendProcessingFailure(var12);
            throw new ProcessingException(Messages.MESSAGES.unableToInvokeRequest(var12.toString()), var12);
        } finally {
            this.cleanUpAfterExecute(httpMethod);
        }

        FinalizedClientResponse response = new FinalizedClientResponse(request.getClientConfiguration(), request.getTracingLogger()) {
            InputStream stream;
            InputStream hc4Stream;

            protected void setInputStream(InputStream is) {
                this.stream = is;
                this.resetEntity();
            }

            public InputStream getInputStream() {
                if (this.stream == null) {
                    HttpEntity entity = res.getEntity();
                    if (entity == null) {
                        return null;
                    }

                    try {
                        this.hc4Stream = entity.getContent();
                        this.stream = CustomApacheHttpClient43Engine.this.createBufferedStream(this.hc4Stream);
                    } catch (IOException var3) {
                        throw new RuntimeException(var3);
                    }
                }

                return this.stream;
            }

            public void releaseConnection() throws IOException {
                this.releaseConnection(true);
            }

            public void releaseConnection(boolean consumeInputStream) throws IOException {
                if (consumeInputStream) {
                    boolean var11 = false;

                    try {
                        var11 = true;
                        if (this.stream != null) {
                            this.stream.close();
                            var11 = false;
                        } else {
                            InputStream is = this.getInputStream();
                            if (is != null) {
                                is.close();
                                var11 = false;
                            } else {
                                var11 = false;
                            }
                        }
                    } finally {
                        if (var11) {
                            if (this.hc4Stream != null) {
                                try {
                                    this.hc4Stream.close();
                                } catch (IOException var13) {
                                }
                            } else {
                                try {
                                    HttpEntity entity = res.getEntity();
                                    if (entity != null) {
                                        entity.getContent().close();
                                    }
                                } catch (IOException var12) {
                                }
                            }

                        }
                    }

                    if (this.hc4Stream != null) {
                        try {
                            this.hc4Stream.close();
                        } catch (IOException var16) {
                        }
                    } else {
                        try {
                            HttpEntity entityx = res.getEntity();
                            if (entityx != null) {
                                entityx.getContent().close();
                            }
                        } catch (IOException var15) {
                        }
                    }
                } else if (res instanceof CloseableHttpResponse) {
                    try {
                        ((CloseableHttpResponse)res).close();
                    } catch (IOException var14) {
                        LogMessages.LOGGER.warn(Messages.MESSAGES.couldNotCloseHttpResponse(), var14);
                    }
                }

            }
        };
        response.setProperties(request.getMutableProperties());
        response.setStatus(res.getStatusLine().getStatusCode());
        response.setReasonPhrase(res.getStatusLine().getReasonPhrase());
        response.setHeaders(extractHeaders(res));
        response.setClientConfiguration(request.getClientConfiguration());
        return response;
    }

    protected HttpRequestBase createHttpMethod(String url, final String restVerb) {
        if ("GET".equals(restVerb)) {
            HttpEntityEnclosingRequestBase base = new HttpEntityEnclosingRequestBase() {
                @Override
                public String getMethod() {
                    return "GET";
                }
            };

            base.setURI(URI.create(url));
            return base;
        } else {
            return "POST".equals(restVerb) ? new HttpPost(url) : new HttpPost(url) {
                public String getMethod() {
                    return restVerb;
                }
            };
        }
    }

    protected void loadHttpMethod(ClientInvocation request, HttpRequestBase httpMethod) throws Exception {
        if (this.isFollowRedirects()) {
            this.setRedirectRequired(request, httpMethod);
        } else {
            this.setRedirectNotRequired(request, httpMethod);
        }

        if (request.getEntity() != null) {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            request.getDelegatingOutputStream().setDelegate(baos);

            try {
                HttpEntity entity = this.buildEntity(request);
                HttpEntityEnclosingRequestBase post = (HttpEntityEnclosingRequestBase)httpMethod;
                this.commitHeaders(request, httpMethod);
                post.setEntity(entity);
            } catch (IOException var6) {
                throw new RuntimeException(var6);
            }
        } else {
            this.commitHeaders(request, httpMethod);
        }

    }

    protected void commitHeaders(ClientInvocation request, HttpRequestBase httpMethod) {
        MultivaluedMap<String, String> headers = request.getHeaders().asMap();
        Iterator var4 = headers.entrySet().iterator();

        while(var4.hasNext()) {
            Map.Entry<String, List<String>> header = (Map.Entry)var4.next();
            List<String> values = (List)header.getValue();
            Iterator var7 = values.iterator();

            while(var7.hasNext()) {
                String value = (String)var7.next();
                httpMethod.addHeader((String)header.getKey(), value);
            }
        }

    }

    public boolean isChunked() {
        return this.chunked;
    }

    public void setChunked(boolean chunked) {
        this.chunked = chunked;
    }

    public boolean isFollowRedirects() {
        return this.followRedirects;
    }

    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    protected void cleanUpAfterExecute(HttpRequestBase httpMethod) {
        if (httpMethod != null && httpMethod instanceof HttpPost) {
            HttpPost postMethod = (HttpPost)httpMethod;
            HttpEntity entity = postMethod.getEntity();
            if (entity != null && entity instanceof FileExposingFileEntity) {
                File tempRequestFile = ((FileExposingFileEntity)entity).getFile();

                try {
                    boolean isDeleted = tempRequestFile.delete();
                    if (!isDeleted) {
                        this.handleFileNotDeletedError(tempRequestFile, (Exception)null);
                    }
                } catch (Exception var6) {
                    this.handleFileNotDeletedError(tempRequestFile, var6);
                }
            }
        }

    }

    protected HttpEntity buildEntity(ClientInvocation request) throws IOException {
        AbstractHttpEntity entityToBuild = null;
        DeferredFileOutputStream memoryManagedOutStream = this.writeRequestBodyToOutputStream(request);
        MediaType mediaType = request.getHeaders().getMediaType();
        if (memoryManagedOutStream.isInMemory()) {
            ByteArrayEntity entityToBuildByteArray = new ByteArrayEntity(memoryManagedOutStream.getData());
            if (mediaType != null) {
                entityToBuildByteArray.setContentType(new BasicHeader("Content-Type", mediaType.toString()));
            }

            entityToBuild = entityToBuildByteArray;
        } else {
            entityToBuild = new FileExposingFileEntity(memoryManagedOutStream.getFile(), mediaType == null ? null : mediaType.toString());
        }

        if (request.isChunked()) {
            ((AbstractHttpEntity)entityToBuild).setChunked(true);
        }

        return (HttpEntity)entityToBuild;
    }

    private DeferredFileOutputStream writeRequestBodyToOutputStream(ClientInvocation request) throws IOException {
        DeferredFileOutputStream memoryManagedOutStream = new DeferredFileOutputStream(this.fileUploadInMemoryThresholdLimit * this.getMemoryUnitMultiplier(), this.getTempfilePrefix(), ".tmp", this.fileUploadTempFileDir);
        request.getDelegatingOutputStream().setDelegate(memoryManagedOutStream);
        request.writeRequestBody(request.getEntityStream());
        memoryManagedOutStream.close();
        return memoryManagedOutStream;
    }

    protected String getTempfilePrefix() {
        return processId;
    }

    private int getMemoryUnitMultiplier() {
        switch (this.fileUploadMemoryUnit) {
            case BY:
                return 1;
            case KB:
                return 1024;
            case MB:
                return 1048576;
            case GB:
                return 1073741824;
            default:
                return 1;
        }
    }

    private void handleFileNotDeletedError(File tempRequestFile, Exception ex) {
        LogMessages.LOGGER.warn(Messages.MESSAGES.couldNotDeleteFile(tempRequestFile.getAbsolutePath()), ex);
        tempRequestFile.deleteOnExit();
    }

    protected HttpClient createDefaultHttpClient() {
        HttpClientBuilder builder = HttpClientBuilder.create();
        RequestConfig.Builder requestBuilder = RequestConfig.custom();
        if (this.defaultProxy != null) {
            requestBuilder.setProxy(this.defaultProxy);
        }

        builder.disableContentCompression();
        builder.setDefaultRequestConfig(requestBuilder.build());
        return builder.build();
    }

    public HttpHost getDefaultProxy() {
        Configurable clientConfiguration = (Configurable)this.httpClient;
        return clientConfiguration.getConfig().getProxy();
    }

    protected void setRedirectRequired(ClientInvocation request, HttpRequestBase httpMethod) {
        RequestConfig.Builder requestBuilder = RequestConfig.copy(this.getCurrentConfiguration(request, httpMethod));
        requestBuilder.setRedirectsEnabled(true);
        httpMethod.setConfig(requestBuilder.build());
    }

    protected void setRedirectNotRequired(ClientInvocation request, HttpRequestBase httpMethod) {
        RequestConfig.Builder requestBuilder = RequestConfig.copy(this.getCurrentConfiguration(request, httpMethod));
        requestBuilder.setRedirectsEnabled(false);
        httpMethod.setConfig(requestBuilder.build());
    }

    private RequestConfig getCurrentConfiguration(ClientInvocation request, HttpRequestBase httpMethod) {
        RequestConfig baseConfig;
        if (httpMethod != null && httpMethod.getConfig() != null) {
            baseConfig = httpMethod.getConfig();
        } else {
            CustomApacheHttpClient43Engine engine = (CustomApacheHttpClient43Engine)request.getClient().httpEngine();
            baseConfig = ((Configurable)engine.getHttpClient()).getConfig();
            if (baseConfig == null) {
                Configurable clientConfiguration = (Configurable)this.httpClient;
                baseConfig = clientConfiguration.getConfig();
            }
        }

        return baseConfig;
    }

    public boolean isClosed() {
        return this.closed;
    }

    public void close() {
        if (!this.closed) {
            if (this.allowClosingHttpClient && this.httpClient != null) {
                try {
                    ((CloseableHttpClient)this.httpClient).close();
                } catch (Exception var2) {
                    throw new RuntimeException(var2);
                }
            }

            this.closed = true;
        }
    }

    private static File getTempDir() {
        if (System.getSecurityManager() == null) {
            Optional<String> value = ConfigurationFactory.getInstance().getConfiguration().getOptionalValue("java.io.tmpdir", String.class);
            return (File)value.map(File::new).orElseGet(() -> {
                return new File(System.getProperty("java.io.tmpdir"));
            });
        } else {
            return (File)AccessController.doPrivileged((PrivilegedAction<File>) () -> {
                Optional<String> value = ConfigurationFactory.getInstance().getConfiguration().getOptionalValue("java.io.tmpdir", String.class);
                return (File)value.map(File::new).orElseGet(() -> {
                    return new File(System.getProperty("java.io.tmpdir"));
                });
            });
        }
    }

    static {
        try {
            processId = (String)AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                public String run() throws Exception {
                    return ManagementFactory.getRuntimeMXBean().getName().replaceAll("[^0-9a-zA-Z]", "");
                }
            });
        } catch (PrivilegedActionException var1) {
            throw new RuntimeException(var1);
        }
    }

    private static class FileExposingFileEntity extends FileEntity {
        FileExposingFileEntity(File pFile, String pContentType) {
            super(pFile, pContentType);
        }

        File getFile() {
            return this.file;
        }
    }
}
