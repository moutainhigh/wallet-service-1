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
public class EthBlockTxParser implements EthParser {

    @Override
    public void parse(EthBlockContext context, EthChainParser chain, String txHash) {
        parseTx(context, txHash);

        chain.parse(context, txHash);
    }

    private void parseTx(EthBlockContext context, String txHash) {
        List<EthTx> txList = context.getTxList();
        EthBlock.Block block = context.getBlock();
        EthBlock.TransactionObject tx = context.getTxMap().get(txHash);
        Optional<TransactionReceipt> receipt = context.getReceiptMap().get(txHash);

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
