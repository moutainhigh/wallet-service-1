package io.jingwei.wallet.biz.sync.eth.listener;

import io.andy.rocketmq.wrapper.core.RMWrapper;
import io.andy.rocketmq.wrapper.core.producer.RMProducer;
import io.jingwei.wallet.biz.entity.EthTx;
import io.jingwei.wallet.biz.service.IEthTxService;
import io.jingwei.wallet.biz.sync.eth.parser.ParserContext;
import io.jingwei.wallet.biz.utils.AsyncTaskService;
import io.jingwei.wallet.biz.utils.ExecutorNameFactory;
import io.jingwei.wallet.biz.utils.SingleThreadedAsyncTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.jingwei.wallet.biz.constant.MessageTopics.PARSE_ETH_COMPLETE_TOPIC;

@Component
@Slf4j
public class DefaultEthParseListener implements EthParseListener {
    private static final String TX_EXECUTOR_NAME = "tx-confirm-executor";

    private AsyncTaskService asyncService        = new SingleThreadedAsyncTaskService();

    private RMProducer                           producer;

    @Autowired
    private IEthTxService                        ethTxService;

    /**
     * 交易确认消息发送的事务消息监听器，从而保证本地数据存储和消息到达broker是原子操作
     */
    @Autowired
    private EthTxListener                        ethTxListener;

    @Value("${tx.confirm.retry:3}")
    private Integer                              sendRetryTimes;

    @Value("${rocketmq.nameServer:127.0.0.1:9876}")
    private String                               nameSrvAddr;

    /**
     *  消费端需要自己处理幂等问题，不排除同一个消息多次投递
     */
    @Override
    public void onComplete(ParserContext context) {
        asyncService.execute(ExecutorNameFactory.build(TX_EXECUTOR_NAME, ""), ()->{
            long currentHeight = context.getBlock().getNumber().longValue();
            List<EthTx> ethTxList =  ethTxService.listConfirmedTx(currentHeight);

            if (CollectionUtils.isNotEmpty(ethTxList)) {
                ethTxList.stream().forEach(ethTx -> sendConfirmedTxMessage(ethTx));
            }
        });
    }

    /**
     *  发送已经被确认的交易通知给消费端，消费端可以更新交易状态或者做账务操作等
     */
    private void sendConfirmedTxMessage(EthTx ethTx) {
        createProducerIfNeed();

        try {
            producer.sendTransactionMessage(ethTx, ethTx);
        } catch (Exception e) {
            log.error("sendTransactionMessage failed, e={}", e);
        }
    }

    private void createProducerIfNeed() {
        if (producer == null) {
            producer = RMWrapper.with(RMProducer.class)
                    .producerGroup(PARSE_ETH_COMPLETE_TOPIC.getProducerGroup())
                    .transactionListener(ethTxListener)
                    .topic(PARSE_ETH_COMPLETE_TOPIC.getTopic())
                    .retryTimes(sendRetryTimes)
                    .nameSrvAddr(nameSrvAddr)
                    .start();
        }
    }

}
