package io.jingwei.wallet.biz.sync.eth.listener;

import io.andy.rocketmq.wrapper.core.producer.RMProducer;
import io.jingwei.wallet.biz.sync.eth.parser.ParserContext;

public interface EthParseListener {

    void onComplete(RMProducer producer, ParserContext context);
}
