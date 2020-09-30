package io.jingwei.wallet.biz.sync.eth.listener;

import io.jingwei.wallet.biz.sync.eth.parser.EthBlockContext;

public interface EthParseListener {

    void onComplete(EthBlockContext context);
}
