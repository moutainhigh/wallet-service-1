package io.jingwei.wallet.biz.sync.eth.listener;

import io.andy.rocketmq.wrapper.core.RMWrapper;
import io.andy.rocketmq.wrapper.core.producer.RMProducer;
import io.jingwei.wallet.biz.config.EthSyncConfig;
import io.jingwei.wallet.biz.entity.EthTx;
import io.jingwei.wallet.biz.service.IEthTxService;
import io.jingwei.wallet.biz.sync.eth.parser.ParserContext;
import io.jingwei.wallet.biz.utils.AsyncTaskService;
import io.jingwei.wallet.biz.utils.SingleThreadedAsyncTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.jingwei.wallet.biz.constant.MessageTopics.ETH_TX_UNCONFIRMED_TOPIC;
import static io.jingwei.wallet.biz.utils.ExecutorNameFactory.build;

@Component
@Slf4j
public class EthTxUnconfirmListener implements EthParseListener {
    private static final String TX_UNCONFIRM_EXECUTOR_NAME = "tx-unconfirmed-executor";

    private AsyncTaskService unconfirmAsyncService         = new SingleThreadedAsyncTaskService();

    private RMProducer                                     producer;

    @Autowired
    private IEthTxService                                  ethTxService;

    /**
     * 交易被挖矿消息发送的事务消息监听器，从而保证本地数据存储和消息到达broker是原子操作
     */
    @Autowired
    private EthTxConfirmTxListener ethTxListener;

    @Value("${confirmed.message.retry:3}")
    private Integer                                        sendRetryTimes;

    @Value("${rocketmq.nameServer:127.0.0.1:9876}")
    private String                                         nameSrvAddr;

    @Autowired
    private EthSyncConfig                                  ethSyncConfig;

    /**
     *  消费端需要自己处理幂等问题，不排除同一个消息多次投递
     */
    @Override
    public void onComplete(ParserContext context) {
        unconfirmAsyncService.execute(build(TX_UNCONFIRM_EXECUTOR_NAME, ethSyncConfig.getNodeName()), ()->{
            long currentHeight = context.getBlock().getNumber().longValue();
            List<EthTx> ethTxList =  ethTxService.listUnconfirmedTx(currentHeight);

            if (CollectionUtils.isNotEmpty(ethTxList)) {
                ethTxList.stream().forEach(ethTx -> sendTxUnconfirmedMessage(ethTx));
            }
        });
    }

    /**
     * 发送已经被挖矿的交易通知给消费端，消费端可以更新交易状态操作等
     * （此处的消息类型为事务消息，尽量保证消息能够到达broker）
     */
    private void sendTxUnconfirmedMessage(EthTx ethTx) {
        createProducerIfNeed();

        try {
            producer.sendTransactionMessage(ethTx, ethTx);
        } catch (Exception e) {
            log.error("send tx confirmed message failed, e={}", e);
        }
    }

    private void createProducerIfNeed() {
        if (producer == null) {
            producer = RMWrapper.with(RMProducer.class)
                    .producerGroup(ETH_TX_UNCONFIRMED_TOPIC.getProducerGroup())
                    .transactionListener(ethTxListener)
                    .topic(ETH_TX_UNCONFIRMED_TOPIC.getTopic())
                    .retryTimes(sendRetryTimes)
                    .nameSrvAddr(nameSrvAddr)
                    .start();
        }
    }

}
