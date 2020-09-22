package io.jingwei.wallet.biz.sync.eth.listener;

import io.andy.rocketmq.wrapper.core.RMWrapper;
import io.andy.rocketmq.wrapper.core.producer.RMProducer;
import io.jingwei.wallet.biz.exception.ParseTxException;
import io.jingwei.wallet.biz.support.EthRpcCall;
import io.jingwei.wallet.biz.sync.eth.parser.EthChainParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import static io.jingwei.wallet.biz.constant.MessageTopics.PARSE_ETH_COMPLETE_TOPIC;


@Component
@Slf4j
public class DefaultEthBlockListener extends AbstractEthBlockListener {

    private static final int SEND_RETRY_TIMES = 3;

    private EthParseListener                  ethParseListener;

    private RMProducer                        parseCompleteProducer;

    @Autowired
    private EthChainParser                    ethChainParser;

    @Autowired
    private EthRpcCall                        ethRpcCall;

    @Value("${rocketmq.nameServer:127.0.0.1:9876}")
    private String                            nameSrvAddr;
    /**
     * 交易解析完成后发送的事务消息监听器，从而保证本地数据存储和消息到达broker是原子操作
     */
    @Autowired
    private EthTxListener parseCompleteTxListener;


    private final AtomicInteger sn            = new AtomicInteger();
    private final Executor taskExecutor       = Executors.newCachedThreadPool((Runnable r) -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("EthTxParserThread-" + sn.incrementAndGet());
        return t;
    });

    @PostConstruct
    private void init() {
        createParseCompleteProducer();
        createParseCompleteListener();
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
        ethParseListener.onComplete(parseCompleteProducer, parserContext);
    }

    private void parsePipeline(EthBlock.TransactionObject tx) {
        Optional<TransactionReceipt> receipt =  ethRpcCall.getReceipt(tx.getHash());
        parserContext.setReceipt(receipt);
        parserContext.setTx(tx);

        ethChainParser.parse(parserContext);
    }

    private void createParseCompleteProducer() {
        parseCompleteProducer = RMWrapper.with(RMProducer.class)
                .producerGroup(PARSE_ETH_COMPLETE_TOPIC.getProducerGroup())
                .transactionListener(parseCompleteTxListener)
                .topic(PARSE_ETH_COMPLETE_TOPIC.getTopic())
                .retryTimes(SEND_RETRY_TIMES)
                .nameSrvAddr(nameSrvAddr)
                .start();
    }

    private void createParseCompleteListener() {
        ethParseListener = new DefaultEthParseListener();
    }

}
