package io.jingwei.wallet.biz.sync.eth.listener;

import io.jingwei.base.utils.exception.BizErr;
import io.jingwei.wallet.biz.entity.EthTx;
import io.jingwei.wallet.biz.service.IEthTxService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * 发送ETH交易被挖矿的事务性消息监听器
 */
@Component
@Slf4j
public class EthTxUnconfirmTxListener implements TransactionListener {


    private EthTx             ethTx;

    @Autowired
    private IEthTxService     ethTxService;


    @Override
    public LocalTransactionState executeLocalTransaction(Message message, Object o) {

        this.ethTx = (EthTx) o;
        Objects.requireNonNull(ethTx);

        try {
            if(!ethTxService.updateUnconfirmNotified(ethTx.getTxHash())) {
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
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
        if (ethTx == null) {
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }

        Optional<EthTx> txOptional = ethTxService.getByHash(ethTx.getTxHash());
        if (!txOptional.isPresent() || !txOptional.get().getUnconfirmNotified()) {
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }

        return LocalTransactionState.COMMIT_MESSAGE;
    }
}
