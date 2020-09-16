package io.jingwei.wallet.biz.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "eth.sync")
@Data
public class EthSyncConfig {
    private String nodeName;
    private String username;
    private String password;
    private String url;

    private Integer maxIdleConnections = 5;
    private Long readTimeout = 60000L;
    private Long connectionTimeout = 5000L;
    private Long keepAliveDuration = 10000L;

    private Long pollingInterval = 10000L;
    private Long maxBlocksToSync;
    private Integer numBlocksToReplay = 12;
    private Long initialStartBlock;
}
