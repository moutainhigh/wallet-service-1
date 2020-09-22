package io.jingwei.wallet.biz.sync.eth.listener;

import io.jingwei.base.utils.exception.BizErr;
import io.jingwei.base.utils.tx.TxTemplateService;
import io.jingwei.wallet.biz.service.IEthBlockService;
import io.jingwei.wallet.biz.service.IEthLatestService;
import io.jingwei.wallet.biz.service.IEthTxService;
import io.jingwei.wallet.biz.sync.eth.parser.ParserContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.util.Objects;

/**
 * 发送ETH解析完成的事务性消息监听器
 */
@Component
@Slf4j
public class EthTxListener implements TransactionListener {

    @Autowired
    private TxTemplateService txTemplateService;

    @Autowired
    private IEthBlockService  blockService;

    @Autowired
    private IEthTxService     ethTxService;

    @Autowired
    private IEthLatestService ethLatestService;

    private ParserContext     parserContext;


    @Override
    public LocalTransactionState executeLocalTransaction(Message message, Object o) {

        this.parserContext = (ParserContext) o;
        Objects.requireNonNull(parserContext);

        try {
            txTemplateService.doInTransaction(() -> {
                ethTxService.saveOrUpdateList(parserContext.getTxList());
                blockService.saveBlock(parserContext.getBlock());
                ethLatestService.updateByNewBlock(parserContext.getBlock());
            });
        } catch (BizErr err) {
            return LocalTransactionState.ROLLBACK_MESSAGE;
        } catch (Exception e) {
            log.error("持久化区块异常,等待回查发起, e={}", e);
            return LocalTransactionState.UNKNOW;
        }

        return LocalTransactionState.COMMIT_MESSAGE;
    }

    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
        EthBlock.Block block = parserContext.getBlock();
        if (block == null) {
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }

        if (blockService.getByHash(block.getHash()) == null) {
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }

        return LocalTransactionState.COMMIT_MESSAGE;
    }
}
