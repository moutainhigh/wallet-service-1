package io.jingwei.wallet.biz.sync.eth;

import io.jingwei.wallet.biz.config.EthSyncConfig;
import io.jingwei.wallet.biz.entity.EthLatest;
import io.jingwei.wallet.biz.exception.ForkException;
import io.jingwei.wallet.biz.service.IEthBlockNumberService;
import io.jingwei.wallet.biz.service.IEthBlockService;
import io.jingwei.wallet.biz.service.IEthRollbackService;
import io.jingwei.wallet.biz.support.EthRpcCall;
import io.jingwei.wallet.biz.support.Web3jClient;
import io.jingwei.wallet.biz.sync.AbstractBlockWatcher;
import io.jingwei.wallet.biz.sync.eth.listener.DefaultEthBlockListener;
import io.jingwei.wallet.biz.sync.eth.listener.EthBlockListener;
import io.jingwei.wallet.biz.utils.AsyncTaskService;
import io.jingwei.wallet.biz.utils.ExecutorNameFactory;
import io.jingwei.wallet.biz.utils.SingleThreadedAsyncTaskService;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class EthBlockWatcher extends AbstractBlockWatcher {
    private static final String            BLOCK_EXECUTOR_NAME = "BLOCK";

    private AsyncTaskService               asyncService = new SingleThreadedAsyncTaskService();

    private AtomicLong                     lastBlockNumberProcessed = new AtomicLong(0);

    private EthBlockListener               blockListener;

    private Disposable                     blockSubscription;

    @Autowired
    private IEthBlockNumberService         ethBlockNumberService;

    @Autowired
    private Web3jClient                    web3jClient;

    @Autowired
    private EthSyncConfig                  ethSyncConfig;

    @Autowired
    private IEthRollbackService            ethRollbackService;

    @Autowired
    private IEthBlockService               ethBlockService;

    @Autowired
    private EthRpcCall                     ethRpcCall;


    @PostConstruct
    private void init() {
        blockListener = new DefaultEthBlockListener();
    }

    @Override
    protected long getStartBlock() {
        return ethBlockNumberService.getStartBlockForNode();
    }

    @Override
    protected void replayBlock(long startBlock) {
        DefaultBlockParameter blockParam = DefaultBlockParameter.valueOf(BigInteger.valueOf(startBlock));

        blockSubscription = web3jClient.getWeb3j()
                .replayPastAndFutureBlocksFlowable(blockParam, true)
                .doOnError((error) -> onError(error))
                .subscribe(block -> triggerListener(block), (error) -> onError(error));
    }

    @Override
    public void stop() {
        web3jClient.getWeb3j().shutdown();

        try {
            if (blockSubscription != null) {
                blockSubscription.dispose();
            }
        } finally {
            blockSubscription = null;
        }
    }

    private void triggerListener(EthBlock blockObject) {
        if (blockObject != null) {
            triggerListener0(blockObject);
        }
    }

    private void triggerListener0(EthBlock ethBlock) {
        asyncService.execute(ExecutorNameFactory.build(BLOCK_EXECUTOR_NAME, ethSyncConfig.getNodeName()), () -> {
            final BigInteger expectedBlock = BigInteger.valueOf(lastBlockNumberProcessed.get()).add(BigInteger.ONE);

            //A lower or equal block is valid due to forking or replaying on failure
            if (lastBlockNumberProcessed.get() > 0 && ethBlock.getBlock().getNumber().compareTo(expectedBlock) > 0) {

                final int missingBlocks = ethBlock.getBlock().getNumber()
                        .subtract(expectedBlock)
                        .intValue();

                log.warn("Missing {} blocks.  Expected {}, got {}.  Catching up...",
                        missingBlocks, expectedBlock, ethBlock.getBlock().getNumber());

                //Get each missing block and process before continuing with block that was passed into method
                for(int i = 0; i < missingBlocks; i++) {
                    final BigInteger nextBlock = expectedBlock.add(BigInteger.valueOf(i));

                    try {
                        log.warn("Retrieving block number {}...", nextBlock);
                        final EthBlock.Block block = getBlockWithNumber(nextBlock);

                        if (block != null) {
                            triggerListener(blockListener, block);
                        }
                        updateLastBlockProcessed(block);
                    } catch (Throwable t) {
                        onError(t);
                    }
                }
            }

            triggerListener(blockListener, ethBlock.getBlock());
            updateLastBlockProcessed(ethBlock.getBlock());
        });
    }

    private void onError(Throwable error) {
        log.error("There was an error when processing a block, disposing block subscription", error);

        stop();
    }

    private EthBlock.Block getBlockWithNumber(BigInteger blockNumber) throws IOException {
        final EthBlock ethBlock = web3jClient.getWeb3j().ethGetBlockByNumber(
                DefaultBlockParameter.valueOf(blockNumber), true).send();

        return ethBlock.getBlock();
    }

    private void updateLastBlockProcessed(EthBlock.Block block) {
        lastBlockNumberProcessed.set(block.getNumber().longValue());
    }

    private void triggerListener(EthBlockListener listener, EthBlock.Block block) {
        try {
            checkChainFork(block);
            listener.onBlock(block);
        } catch (Throwable t) {
            onError(t);
        }
    }

    private void checkChainFork(EthBlock.Block block) {
        if (hasChainFork(block)) {
            Long forkRoot = getForkRoot(block.getParentHash());
            ethRollbackService.rollback(forkRoot);

            log.warn("Block has a fork when height={}", forkRoot);
            throw new ForkException("Block has a fork when height equals: " + forkRoot);
        }
    }

    private Long getForkRoot(String parentHash) {
        io.jingwei.wallet.biz.entity.EthBlock block;
        while ((block = ethBlockService.getByHash(parentHash)) == null) {
            parentHash = ethRpcCall.getBlockHash(parentHash);
        }

        return block.getBlockHeight();
    }

    private boolean hasChainFork(EthBlock.Block block) {
        EthLatest preBlock = ethBlockNumberService.getLatestBlock().get();

        return preBlock != null && !StringUtils.equals(block.getParentHash(), preBlock.getHash());
    }


}
