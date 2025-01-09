package com.mpcamargo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.HttpResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import javax.net.ssl.SSLContext;
import javax.net.ssl.HostnameVerifier;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.List;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;
import org.jboss.resteasy.client.jaxrs.internal.FinalizedClientResponse;
import org.jboss.resteasy.util.CaseInsensitiveMap;

public class CustomHttpClient implements ClientHttpEngine {

    private final CloseableHttpClient httpClient;

    public CustomHttpClient() {
        // Configuração de SSL
        SSLContext sslContext = createSslContext();
        HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE; // Ignora verificações de hostname, use com cautela

        // Configurações de conexão HTTP
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000) // Timeout de conexão
                .setSocketTimeout(5000) // Timeout de resposta
                .build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100); // Limite de conexões totais
        connectionManager.setDefaultMaxPerRoute(20); // Limite por rota

        // Inicializando HttpClient
        httpClient = getHttpClient(sslContext, hostnameVerifier, requestConfig, connectionManager);
    }

    private static CloseableHttpClient getHttpClient(SSLContext sslContext, HostnameVerifier hostnameVerifier,
                                                     RequestConfig requestConfig,
                                                     PoolingHttpClientConnectionManager connectionManager) {
        return HttpClients.custom()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(hostnameVerifier)
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                .build();
    }

    @Override
    public SSLContext getSslContext() {
        return createSslContext();
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return null;
    }

    @Override
    public Response invoke(Invocation invocation) {
        ClientInvocation request = (ClientInvocation) invocation;
        // Criação do HttpRequest baseado na Invocation
        HttpRequestBase httpRequest = createHttpRequest(request);

        // Executando a requisição usando o HttpClient
        final CloseableHttpResponse httpResponse;

        try {
            httpResponse = httpClient.execute(httpRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FinalizedClientResponse response = new FinalizedClientResponse(request.getClientConfiguration(), request.getTracingLogger()) {
            InputStream stream;

            protected void setInputStream(InputStream is) {
                this.stream = is;
            }

            public InputStream getInputStream() {
                if (stream == null) {
                    try {
                        if (httpResponse.getEntity() == null) {
                            return null;
                        }

                        stream = new BufferedInputStream(httpResponse.getEntity().getContent(), 8192);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                return stream;
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
                            if (this.stream != null) {
                                try {
                                    this.stream.close();
                                } catch (IOException var13) {
                                }
                            } else {
                                try {
                                    HttpEntity entity = httpResponse.getEntity();
                                    if (entity != null) {
                                        entity.getContent().close();
                                    }
                                } catch (IOException var12) {
                                }
                            }

                        }
                    }

                    if (this.stream != null) {
                        try {
                            this.stream.close();
                        } catch (IOException var16) {
                        }
                    } else {
                        try {
                            HttpEntity entityx = httpResponse.getEntity();
                            if (entityx != null) {
                                entityx.getContent().close();
                            }
                        } catch (IOException var15) {
                        }
                    }
                }

                httpResponse.close();
            }
        };
        response.setProperties(request.getMutableProperties());
        response.setStatus(httpResponse.getStatusLine().getStatusCode());
        response.setHeaders(extractHeaders(httpResponse));

        return response;
    }

    public static CaseInsensitiveMap<String> extractHeaders(HttpResponse response) {
        CaseInsensitiveMap<String> headers = new CaseInsensitiveMap<>();
        Header[] var2 = response.getAllHeaders();
        int var3 = var2.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            Header header = var2[var4];
            headers.add(header.getName(), header.getValue());
        }

        return headers;
    }

    private HttpRequestBase createHttpRequest(ClientInvocation request) {
        String method = request.getMethod();
        URI uri = request.getUri();

        HttpRequestBase httpRequest;

        switch (method.toUpperCase()) {
            case "GET":
                httpRequest = new HttpEntityEnclosingRequestBase() {
                    @Override
                    public String getMethod() {
                        return "GET";
                    }
                };
                httpRequest.setURI(uri);
                break;
            case "POST":
                httpRequest = new org.apache.http.client.methods.HttpPost(uri);
                break;
            case "PUT":
                httpRequest = new org.apache.http.client.methods.HttpPut(uri);
                break;
            case "DELETE":
                httpRequest = new org.apache.http.client.methods.HttpDelete(uri);
                break;
            default:
                throw new UnsupportedOperationException("Method not supported: " + method);
        }

        if (request.getEntity() != null) {
            try {
                ((HttpEntityEnclosingRequestBase) httpRequest).setEntity(
                        new StringEntity(new ObjectMapper().writeValueAsString(request.getEntity()),
                                ContentType.APPLICATION_JSON));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        // Adicionando os headers da requisição RestEasy ao HttpRequest
        for (Map.Entry<String, List<String>> header : request.getHeaders().asMap().entrySet()) {
            for (String value : header.getValue()) {
                httpRequest.addHeader(header.getKey(), value);
            }
        }

        return httpRequest;
    }

    private SSLContext createSslContext() {
        return null;
    }

    @Override
    public void close() {
        try {
            // Libere os recursos do HttpClient
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error closing HttpClient", e);
        }
    }

    // Métodos de redirecionamento (opcionais, lançando UnsupportedOperationException como padrão)
    @Override
    public boolean isFollowRedirects() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFollowRedirects(boolean followRedirects) {
        throw new UnsupportedOperationException();
    }

}
