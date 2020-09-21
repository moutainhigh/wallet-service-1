package io.jingwei.wallet.biz.sync.eth.parser;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EthChainParser {

    private int index = 0;
    private List<EthParser> parsers = new ArrayList<>();

    public EthChainParser addParser(EthParser parser) {
        parsers.add(parser);
        return this;
    }

    public EthChainParser removeParser(EthParser parser) {
        parsers.remove(parser);
        return this;
    }

    public void parse(ParserContext context) {
        if (index == parsers.size()) return;

        EthParser parser = parsers.get(index);
        index++;
        parser.parse(context, this);
    }
}
