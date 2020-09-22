package io.jingwei.wallet.biz.sync.eth.listener;

import io.jingwei.wallet.biz.sync.eth.parser.EthBlockParser;
import io.jingwei.wallet.biz.sync.eth.parser.EthChainParser;
import io.jingwei.wallet.biz.sync.eth.parser.ParserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.web3j.protocol.core.methods.response.EthBlock;

public  abstract class AbstractEthBlockListener implements EthBlockListener {

    @Autowired
    private EthChainParser                   ethChainParser;

    protected ParserContext                  parserContext;

    @Override
    public void onBlock(EthBlock.Block block) {
        createContext(block);
        addTxParsers();
        parseTxAsync();
        parseComplete();
    }

    protected void createContext(EthBlock.Block block) {
        this.parserContext =  new ParserContext().setBlock(block);
    }

    protected void addTxParsers() {
        ethChainParser.addParser(new EthBlockParser());
    }


    protected abstract void parseTxAsync();

    protected abstract void parseComplete();
}
