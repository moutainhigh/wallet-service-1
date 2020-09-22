package io.jingwei.wallet.biz.sync.eth.listener;

import io.jingwei.wallet.biz.sync.eth.parser.ParserContext;

public interface EthParseListener {

    void onComplete(ParserContext context);
}
