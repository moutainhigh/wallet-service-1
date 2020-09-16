package io.jingwei.wallet.biz.sync.eth.parser;


public interface EthParser {
    void parse(ParserContext context, EthChainParser chain);
}
