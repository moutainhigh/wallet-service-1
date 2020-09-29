package io.jingwei.wallet.biz.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.jingwei.wallet.biz.entity.EthTx;
import io.jingwei.wallet.biz.mapper.EthTxMapper;
import io.jingwei.wallet.biz.service.IEthTxService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Andy
 * @since 2020-06-02
 */
@Service
public class EthTxServiceImpl extends ServiceImpl<EthTxMapper, EthTx> implements IEthTxService {
    @Value("${eth.confirmed.height:6}")
    private Integer                            ethConfirmedHeight;

    @Override
    public void saveOrUpdateList(Collection<EthTx> entityList) {
        if (CollectionUtils.isEmpty(entityList)) {
            return;
        }

        entityList.stream().forEach(txInfo -> saveOrUpdate(txInfo,
                Wrappers.<EthTx>lambdaUpdate()
                        .eq(EthTx::getTxHash, txInfo.getTxHash())));

    }

    @Override
    public List<EthTx> listConfirmedTx(long currentHeight) {
        return lambdaQuery()
                .le(EthTx::getBlockHeight, currentHeight - ethConfirmedHeight)
                .eq(EthTx::getConfirmNotified, false)
                .list();
    }

    @Override
    public List<EthTx> listUnconfirmedTx(long currentHeight) {
        return lambdaQuery()
                .le(EthTx::getBlockHeight, currentHeight)
                .eq(EthTx::getUnconfirmNotified, false)
                .list();
    }

    @Override
    public boolean updateConfirmNotified(String txHash) {
        return lambdaUpdate()
                .set(EthTx::getConfirmNotified, true)
                .eq(EthTx::getTxHash, txHash)
                .update();
    }

    @Override
    public boolean updateUnconfirmNotified(String txHash) {
        return lambdaUpdate()
                .set(EthTx::getUnconfirmNotified, true)
                .eq(EthTx::getTxHash, txHash)
                .update();
    }

    @Override
    public Optional<EthTx> getByHash(String txHash) {
        return lambdaQuery().eq(EthTx::getTxHash, txHash).oneOpt();
    }
}
