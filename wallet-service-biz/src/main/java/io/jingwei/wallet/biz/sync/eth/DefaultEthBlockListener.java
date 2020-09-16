package io.jingwei.wallet.biz.sync.eth;

import io.jingwei.base.utils.tx.TxTemplateService;
import io.jingwei.wallet.biz.entity.EthLatest;
import io.jingwei.wallet.biz.exception.ForkException;
import io.jingwei.wallet.biz.service.IEthBlockNumberService;
import io.jingwei.wallet.biz.service.IEthBlockService;
import io.jingwei.wallet.biz.service.IEthRollbackService;
import io.jingwei.wallet.biz.service.IEthTxService;
import io.jingwei.wallet.biz.sync.eth.parser.EthBlockParser;
import io.jingwei.wallet.biz.sync.eth.parser.EthChainParser;
import io.jingwei.wallet.biz.sync.eth.parser.ParserContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Component
@Slf4j
public class DefaultEthBlockListener implements EthBlockListener {

    private volatile long                 lastReplayBlock = 0L;

    @Autowired
    private IEthRollbackService            ethRollbackService;

    @Autowired
    private IEthBlockService               ethBlockService;

    @Autowired
    private IEthBlockNumberService         ethBlockNumberService;

    @Autowired
    private EthChainParser                 ethChainParser;

    @Autowired
    private TxTemplateService              txTemplateService;

    @Autowired
    private IEthBlockService               blockService;

    @Autowired
    private IEthTxService                  ethTxService;

    @Autowired
    private EthRpcCall                     ethRpcCall;


    private final AtomicInteger sn         = new AtomicInteger();
    private final Executor taskExecutor    = Executors.newCachedThreadPool((Runnable r) -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("EthTxParserThread-" + sn.incrementAndGet());
        return t;
    });


    @Override
    public void before(EthBlock.Block block) {
        final long currentBlock = block.getNumber().longValue();

        if (currentBlock < lastReplayBlock) {
            throw new IllegalStateException("Current height must bigger than last one!!");
        }

        log.debug("Block current height={}", currentBlock);

        if (hasChainFork(block)) {
            Long forkRoot = getForkRoot(block.getParentHash());
            ethRollbackService.rollback(forkRoot);

            log.warn("Block has a fork when height={}", forkRoot);
            throw new ForkException("Block has a fork when height equals: " + forkRoot);
        }

        lastReplayBlock = currentBlock;
    }

    @Override
    public void after(EthBlock.Block block) {
        ParserContext context = createContext(block);

        ethChainParser.addParser(new EthBlockParser());

        parseTxAsync(context);
        parseComplete(context);
    }

    private boolean hasChainFork(EthBlock.Block block) {
        EthLatest preBlock = ethBlockNumberService.getLatestBlock().get();

        return preBlock != null && !StringUtils.equals(block.getParentHash(), preBlock.getHash());
    }

    private Long getForkRoot(String parentHash) {
        io.jingwei.wallet.biz.entity.EthBlock block;
        while ((block = ethBlockService.getByHash(parentHash)) == null) {
            parentHash = ethRpcCall.getBlockHash(parentHash);
        }

        return block.getBlockHeight();
    }

    private ParserContext createContext(EthBlock.Block block) {
        return new ParserContext().setBlock(block);
    }

    private void parseTxAsync(ParserContext context) {
        EthBlock.Block block = context.getBlock();
        List<EthBlock.TransactionObject> transactionObjects = block.getTransactions().stream()
                .map(txResult -> (EthBlock.TransactionObject) txResult.get())
                .collect(Collectors.toList());

        boolean hasTx = CollectionUtils.isNotEmpty(transactionObjects);

        if (hasTx) {
            List<CompletableFuture> futures = transactionObjects.stream()
                    .map(tx->CompletableFuture.runAsync(()-> parsePipeline(context, tx), taskExecutor))
                    .collect(Collectors.toList());

            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();
            } catch (Exception e) {
                log.error("Eth block parsing, error={}", e);
            }
        }
    }

    private void parsePipeline(ParserContext context, EthBlock.TransactionObject tx) {
        Optional<TransactionReceipt> receipt =  ethRpcCall.getReceipt(tx.getHash());
        context.setReceipt(receipt);
        context.setTx(tx);

        ethChainParser.parse(context);
    }

    private void parseComplete(ParserContext context) {
        txTemplateService.doInTransaction(() -> {
            ethTxService.saveOrUpdateList(context.getTxList());
            blockService.saveBlock(context.getBlock());
        });
    }

}
