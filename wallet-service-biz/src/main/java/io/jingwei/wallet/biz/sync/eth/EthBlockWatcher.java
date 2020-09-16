package io.jingwei.wallet.biz.sync.eth;

import io.jingwei.wallet.biz.config.EthSyncConfig;
import io.jingwei.wallet.biz.service.IEthBlockNumberService;
import io.jingwei.wallet.biz.sync.AbstractBlockWatcher;
import io.jingwei.wallet.biz.utils.AsyncTaskService;
import io.jingwei.wallet.biz.utils.ExecutorNameFactory;
import io.jingwei.wallet.biz.utils.SingleThreadedAsyncTaskService;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class EthBlockWatcher extends AbstractBlockWatcher {
    private static final String       BLOCK_EXECUTOR_NAME = "BLOCK";

    private AsyncTaskService          asyncService = new SingleThreadedAsyncTaskService();

    private AtomicLong                lastBlockNumberProcessed = new AtomicLong(0);

    private Collection<EthBlockListener> blockListeners = new ConcurrentLinkedQueue<>();

    private Disposable                blockSubscription;

    @Autowired
    private IEthBlockNumberService ethBlockNumberService;

    @Autowired
    private Web3jClient               web3jClient;

    @Autowired
    private EthSyncConfig             ethSyncConfig;


    @PostConstruct
    private void init() {
        blockListeners.add(new DefaultEthBlockListener());
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
                .subscribe(block -> triggerListeners(block), (error) -> onError(error));
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

    private void triggerListeners(EthBlock blockObject) {
        if (blockObject != null) {
            triggerListeners0(blockObject);
        }
    }

    private void triggerListeners0(EthBlock ethBlock) {
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
                            blockListeners.forEach(listener -> triggerListener(listener, block));
                        }
                        updateLastBlockProcessed(block);
                    } catch (Throwable t) {
                        onError(t);
                    }
                }
            }

            blockListeners.forEach(listener -> triggerListener(listener, ethBlock.getBlock()));
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
            listener.before(block);
            listener.after(block);
        } catch (Throwable t) {
            onError(t);
        }
    }


}
