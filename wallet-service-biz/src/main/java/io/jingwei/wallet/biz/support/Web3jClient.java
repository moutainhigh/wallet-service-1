package io.jingwei.wallet.biz.support;

import io.jingwei.wallet.biz.config.EthSyncConfig;
import lombok.Getter;
import okhttp3.ConnectionPool;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WalletWebSocketService;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.utils.Async;

import javax.annotation.PostConstruct;
import javax.xml.bind.DatatypeConverter;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Getter
public class Web3jClient {
    @Autowired
    private EthSyncConfig ethSyncConfig;

    private Web3j web3j;

    private Web3jService web3jService;

    @PostConstruct void init() {
        this.web3j = buildWeb3j(buildWeb3jService());
    }

    private Web3jService buildWeb3jService() {
        Map<String, String> authHeaders;
        if (ethSyncConfig.getUsername() != null && ethSyncConfig.getPassword() != null) {
            authHeaders = new HashMap<>();
            authHeaders.put(
                    "Authorization",
                    "Basic " + DatatypeConverter.printBase64Binary(
                            String.format("%s:%s", ethSyncConfig.getUsername(), ethSyncConfig.getPassword()).getBytes()));
        } else {
            authHeaders = null;
        }

        if (isWebSocketUrl(ethSyncConfig.getUrl())) {
            final URI uri = parseURI(ethSyncConfig.getUrl());

            final WebSocketClient client = authHeaders != null ? new WebSocketClient(uri, authHeaders) : new WebSocketClient(uri);

            WebSocketService wsService = new WalletWebSocketService(client, false);

            try {
                wsService.connect();
            } catch (ConnectException e) {
                throw new RuntimeException("Unable to connect to eth node websocket", e);
            }

            web3jService = wsService;
        } else {

            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

            ConnectionPool pool = new ConnectionPool(ethSyncConfig.getMaxIdleConnections(),
                    ethSyncConfig.getKeepAliveDuration(), TimeUnit.MILLISECONDS);
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectionPool(pool)
                    . cookieJar(new JavaNetCookieJar(cookieManager))
                    .readTimeout(ethSyncConfig.getReadTimeout(),TimeUnit.MILLISECONDS)
                    .connectTimeout(ethSyncConfig.getConnectionTimeout(),TimeUnit.MILLISECONDS)
                    .build();
            HttpService httpService = new HttpService(ethSyncConfig.getUrl(),client,false);
            if (authHeaders != null) {
                httpService.addHeaders(authHeaders);
            }
            web3jService = httpService;
        }

        return web3jService;
    }

    private Web3j buildWeb3j(Web3jService web3jService) {

        return Web3j.build(web3jService, ethSyncConfig.getPollingInterval(), Async.defaultExecutorService());
    }

    private boolean isWebSocketUrl(String nodeUrl) {
        return nodeUrl.contains("wss://") || nodeUrl.contains("ws://");
    }

    private URI parseURI(String serverUrl) {
        try {
            return new URI(serverUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(String.format("Failed to parse URL: '%s'", serverUrl), e);
        }
    }
}
