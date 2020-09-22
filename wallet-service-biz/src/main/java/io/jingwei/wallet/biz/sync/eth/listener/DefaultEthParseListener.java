package io.jingwei.wallet.biz.sync.eth.listener;

import io.andy.rocketmq.wrapper.core.producer.RMProducer;
import io.jingwei.wallet.biz.sync.eth.parser.ParserContext;
import io.jingwei.wallet.biz.utils.AsyncTaskService;
import io.jingwei.wallet.biz.utils.ExecutorNameFactory;
import io.jingwei.wallet.biz.utils.SingleThreadedAsyncTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DefaultEthParseListener implements EthParseListener {
    private static final String TX_EXECUTOR_NAME = "";

    private AsyncTaskService asyncService = new SingleThreadedAsyncTaskService();



    @Override
    public void onComplete(RMProducer producer, ParserContext context) {
        asyncService.execute(ExecutorNameFactory.build(TX_EXECUTOR_NAME, ""), ()->{
            try {
                producer.sendTransactionMessage(context.getBlock(), context);
            } catch (Exception e) {
                log.error("sendTransactionMessage failed, e={}", e);
            }
        });
    }
}
