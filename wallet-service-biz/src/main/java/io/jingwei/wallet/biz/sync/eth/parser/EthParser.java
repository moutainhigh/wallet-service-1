package io.jingwei.wallet.biz.sync.eth.parser;


public interface EthParser {
    void parse(EthBlockContext context, EthChainParser chain, String txHash);
}
