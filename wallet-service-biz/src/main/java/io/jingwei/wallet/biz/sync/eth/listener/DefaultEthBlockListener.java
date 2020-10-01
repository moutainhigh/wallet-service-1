package io.jingwei.wallet.biz.sync.eth.listener;

import io.jingwei.base.utils.tx.TxTemplateService;
import io.jingwei.wallet.biz.exception.ParseTxException;
import io.jingwei.wallet.biz.service.IEthBlockService;
import io.jingwei.wallet.biz.service.IEthLatestService;
import io.jingwei.wallet.biz.service.IEthTxService;
import io.jingwei.wallet.biz.support.EthRpcCall;
import io.jingwei.wallet.biz.sync.eth.parser.EthChainParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
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

@Scope("prototype")
@Component
@Slf4j
public class DefaultEthBlockListener extends AbstractEthBlockListener {

    /**
     * 交易解析完成的时候执行的listener
     */
    @Autowired
    private List<EthParseListener>            ethParseListeners;

    @Autowired
    private EthChainParser                    ethChainParser;

    @Autowired
    private EthRpcCall                        ethRpcCall;

    @Autowired
    private TxTemplateService                 txTemplateService;

    @Autowired
    private IEthBlockService                  blockService;

    @Autowired
    private IEthTxService                     ethTxService;

    @Autowired
    private IEthLatestService                 ethLatestService;


    private final AtomicInteger sn            = new AtomicInteger();
    private final Executor taskExecutor       = Executors.newCachedThreadPool((Runnable r) -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("EthTxParserThread-" + sn.incrementAndGet());
        return t;
    });

    @Override
    protected void parseTxAsync() {
        EthBlock.Block block = ethBlockContext.getBlock();
        List<EthBlock.TransactionObject> transactionObjects = block.getTransactions().stream()
                .map(txResult -> (EthBlock.TransactionObject) txResult.get())
                .collect(Collectors.toList());

        boolean hasTx = CollectionUtils.isNotEmpty(transactionObjects);

        if (hasTx) {
            List<CompletableFuture> futures = transactionObjects.stream()
                    .map(tx->CompletableFuture.runAsync(()-> parsePipeline(tx), taskExecutor))
                    .collect(Collectors.toList());

            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();
            } catch (Exception e) {
                log.error("Eth block parsing, error={}", e);
                throw new ParseTxException("Eth block parsing error!!");
            }
        }
    }

    @Override
    protected void parseComplete() {
        txTemplateService.doInTransaction(() -> {
            ethTxService.saveOrUpdateList(ethBlockContext.getTxList());
            blockService.saveBlock(ethBlockContext.getBlock());
            ethLatestService.updateByNewBlock(ethBlockContext.getBlock());
        });

        ethParseListeners.stream().forEach(listener -> listener.onComplete(ethBlockContext));
    }

    private void parsePipeline(EthBlock.TransactionObject tx) {
        Optional<TransactionReceipt> receipt =  ethRpcCall.getReceipt(tx.getHash());
        ethBlockContext.getReceiptMap().putIfAbsent(tx.getHash(), receipt);
        ethBlockContext.getTxMap().putIfAbsent(tx.getHash(), tx);

        ethChainParser.parse(ethBlockContext, tx.getHash());
    }


}
