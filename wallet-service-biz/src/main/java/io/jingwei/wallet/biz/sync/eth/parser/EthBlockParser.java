package io.jingwei.wallet.biz.sync.eth.parser;

import io.jingwei.wallet.biz.entity.EthTx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.util.List;
import java.util.Optional;

import static io.jingwei.wallet.biz.utils.EthUtils.fromWei;

@Scope("prototype")
@Component
@Slf4j
public class EthBlockParser implements EthParser {

    @Override
    public void parse(ParserContext context, EthChainParser chain) {
        parseTx(context);

        chain.parse(context);
    }

    private void parseTx(ParserContext context) {
        List<EthTx> txList = context.getTxList();
        EthBlock.Block block = context.getBlock();
        EthBlock.TransactionObject tx = context.getTx();
        Optional<TransactionReceipt> receipt = context.getReceipt();

        parseTx0(txList, block, tx, receipt);
    }

    private void parseTx0(List<EthTx> txList, EthBlock.Block block, EthBlock.TransactionObject tx ,
                         Optional<TransactionReceipt> receipt) {
        if (receipt.isPresent()) {
            EthTx ethTx = new EthTx().setTxHash(tx.getHash())
                    .setSuccess(receipt.get().isStatusOK())
                    .setTxIndex(tx.getTransactionIndex().intValue())
                    .setAmount(fromWei(tx.getValue()))
                    .setBlockHeight(tx.getBlockNumber().longValue())
                    .setFeeUsed(tx.getGas().longValue())
                    .setFeePrice(tx.getGasPrice().longValue())
                    .setBlockTime(block.getTimestamp().longValue())
                    .setFromAddress(tx.getFrom())
                    .setToAddress(tx.getTo());

            txList.add(ethTx);
        } else {
            log.warn("Tx={}, receipt is not present", tx.getHash());
        }
    }

}
