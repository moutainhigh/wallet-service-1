

package io.jingwei.wallet.biz.service.impl;

import io.jingwei.wallet.biz.config.EthSyncConfig;
import io.jingwei.wallet.biz.entity.EthLatest;
import io.jingwei.wallet.biz.service.IEthBlockNumberService;
import io.jingwei.wallet.biz.service.IEthLatestService;
import io.jingwei.wallet.biz.sync.eth.EthRpcCall;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class EthBlockNumberServiceImpl implements IEthBlockNumberService {

    @Autowired
    private IEthLatestService       ethLatestService;

    @Autowired
    private EthSyncConfig           ethSyncConfig;

    @Autowired
    private EthRpcCall              ethRpcCall;

    @Override
    public long getStartBlockForNode() {
        final String nodeName = ethSyncConfig.getNodeName();
        final Optional<EthLatest> latestBlock = getLatestBlock();

        if (latestBlock.isPresent()) {

            //The last block processed
            final long latestBlockNumber = latestBlock.get().getHeight();
            log.info("Last block number processed on node {}: {}", nodeName, latestBlockNumber);

            int replayNum = ethSyncConfig.getNumBlocksToReplay();
            long startBlock = latestBlockNumber - replayNum;

            //Check the replay subtraction result is positive
            startBlock =  startBlock > 0 ? startBlock : 1;

            log.info("Start block number for node {}: {}", nodeName, startBlock);
            return startBlock;
        }

        final Long initialStartBlock = ethSyncConfig.getInitialStartBlock();

        final long startBlock = initialStartBlock != null ? initialStartBlock : getCurrentBlockAtStartup();

        log.info("Start block number for node {}: {}", nodeName, startBlock);

        return startBlock;
    }

    @Override
    public Optional<EthLatest> getLatestBlock() {
        final String nodeName = ethSyncConfig.getNodeName();
        return ethLatestService.getLatestBlock(nodeName);
    }

    private long getCurrentBlockAtStartup() {
        return ethRpcCall.getBlockHeight();
    }
}
