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
                .eq(EthTx::getNotified, false)
                .list();
    }

    @Override
    public void updateTxNotified(String txHash) {
        lambdaUpdate()
                .set(EthTx::getNotified, true)
                .eq(EthTx::getTxHash, txHash)
                .update();
    }

    @Override
    public Optional<EthTx> getByHash(String txHash) {
        return lambdaQuery().eq(EthTx::getTxHash, txHash).oneOpt();
    }
}
