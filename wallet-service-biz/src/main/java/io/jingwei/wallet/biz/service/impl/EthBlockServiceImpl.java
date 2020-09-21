package io.jingwei.wallet.biz.service.impl;

import io.jingwei.wallet.biz.entity.EthBlock;
import io.jingwei.wallet.biz.mapper.EthBlockMapper;
import io.jingwei.wallet.biz.service.IEthBlockService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Andy
 * @since 2020-06-02
 */
@Service
public class EthBlockServiceImpl extends ServiceImpl<EthBlockMapper, EthBlock> implements IEthBlockService {

    @Override
    public void saveBlock(org.web3j.protocol.core.methods.response.EthBlock.Block block) {

    }

    @Override
    public void deleteGreaterBlock(long blockHeight) {
        lambdaUpdate()
                .gt(EthBlock::getBlockHeight, blockHeight)
                .remove();
    }

    @Override
    public EthBlock getByHash(String blockHash) {
        return lambdaQuery()
                .eq(EthBlock::getBlockHash, blockHash)
                .eq(EthBlock::getDeleted, false)
                .one();
    }

    @Override
    public EthBlock getByHeight(long height) {
        return lambdaQuery()
                .eq(EthBlock::getBlockHeight, height)
                .one();
    }
}
