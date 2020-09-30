package io.jingwei.wallet.biz.sync.eth.listener;

import io.jingwei.wallet.biz.sync.eth.parser.EthBlockTxParser;
import io.jingwei.wallet.biz.sync.eth.parser.EthChainParser;
import io.jingwei.wallet.biz.sync.eth.parser.EthBlockContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.web3j.protocol.core.methods.response.EthBlock;

public  abstract class AbstractEthBlockListener implements EthBlockListener {

    @Autowired
    private EthChainParser                   ethChainParser;

    protected EthBlockContext                ethBlockContext;

    @Override
    public void onBlock(EthBlock.Block block) {
        createContext(block);
        addTxParsers();
        parseTxAsync();
        parseComplete();
    }

    protected void createContext(EthBlock.Block block) {
        this.ethBlockContext =  new EthBlockContext().setBlock(block);
    }

    protected void addTxParsers() {
        ethChainParser.addParser(new EthBlockTxParser());
    }


    protected abstract void parseTxAsync();

    protected abstract void parseComplete();
}
