package io.jingwei.wallet.biz.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.jingwei.wallet.biz.entity.EthTx;
import io.jingwei.wallet.biz.mapper.EthTxMapper;
import io.jingwei.wallet.biz.service.IEthTxService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

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
    private static final int CONFIRMED_HEIGHT = 6;

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
                .le(EthTx::getBlockHeight, currentHeight - CONFIRMED_HEIGHT)
                .eq(EthTx::getProcessed, false)
                .list();
    }
}