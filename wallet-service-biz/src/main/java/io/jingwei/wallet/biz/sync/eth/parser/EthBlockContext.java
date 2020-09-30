package io.jingwei.wallet.biz.sync.eth.parser;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.jingwei.wallet.biz.entity.EthTx;
import lombok.Data;
import lombok.experimental.Accessors;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@Accessors(chain = true)
public class EthBlockContext {
    private EthBlock.Block block;
    private List<EthTx> txList = Lists.newArrayList();
    private Map<String, Optional<TransactionReceipt>> receiptMap = Maps.newConcurrentMap();
    private Map<String,EthBlock.TransactionObject> txMap = Maps.newConcurrentMap();

}
