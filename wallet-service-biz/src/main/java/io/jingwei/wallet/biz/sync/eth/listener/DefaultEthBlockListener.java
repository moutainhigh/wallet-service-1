package io.jingwei.wallet.biz.sync.eth.listener;
//x
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
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Component
@Slf4j
public class DefaultEthBlockListener extends AbstractEthBlockListener {

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

    @Autowired
    private IEthLatestService              ethLatestService;

    //private RMProducer                     rmProducer;


    private final AtomicInteger sn         = new AtomicInteger();
    private final Executor taskExecutor    = Executors.newCachedThreadPool((Runnable r) -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("EthTxParserThread-" + sn.incrementAndGet());
        return t;
    });

    @PostConstruct
    private void init() {
//        rmProducer = RMWrapper.with(RMProducer.class)
//                .producerGroup("")
//                .topic("")
//                .retryTimes(3)
//                .nameSrvAddr("")
//                .transactionListener(null)
//                .start();
    }

    @Override
    protected void parseTxAsync() {
        EthBlock.Block block = parserContext.getBlock();
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
            ethTxService.saveOrUpdateList(parserContext.getTxList());
            blockService.saveBlock(parserContext.getBlock());
            ethLatestService.updateByNewBlock(parserContext.getBlock());
        });


    }


    private void parsePipeline(EthBlock.TransactionObject tx) {
        Optional<TransactionReceipt> receipt =  ethRpcCall.getReceipt(tx.getHash());
        parserContext.setReceipt(receipt);
        parserContext.setTx(tx);

        ethChainParser.parse(parserContext);
    }

}
