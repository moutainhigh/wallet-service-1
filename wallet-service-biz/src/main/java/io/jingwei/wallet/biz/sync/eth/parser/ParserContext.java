package io.jingwei.wallet.biz.sync.eth.parser;

import com.google.common.collect.Lists;
import io.jingwei.wallet.biz.entity.EthTx;
import lombok.Data;
import lombok.experimental.Accessors;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.util.List;
import java.util.Optional;

@Data
@Accessors(chain = true)
public class ParserContext {
    private EthBlock.Block block;
    private List<EthTx> txList = Lists.newArrayList();
    private Optional<TransactionReceipt> receipt;
    private EthBlock.TransactionObject tx;

}
