package io.jingwei.wallet.biz.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;

@Configuration
@Data
public class SyncConfig {
    /**
     * the interval for waiting block generation
     */
    @Value("${sync.interval:3000}")
    public int SYNC_BLOCK_INTERVAL;


    /**
     * the maximum time of each node for waiting for generating block
     */
    @Value("${sync.block-stop-increase:10}")
    public int SYNC_BLOCK_STOP_INCREASE;


    @Value("${sync.eth-nodes:127.0.0.1:8545}")
    private String nodes;
}
