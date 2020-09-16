package io.jingwei.wallet.biz.service.impl;

import io.jingwei.wallet.biz.config.EthSyncConfig;
import io.jingwei.wallet.biz.entity.EthBlock;
import io.jingwei.wallet.biz.entity.EthLatest;
import io.jingwei.wallet.biz.mapper.EthLatestMapper;
import io.jingwei.wallet.biz.service.IEthBlockService;
import io.jingwei.wallet.biz.service.IEthLatestService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Andy
 * @since 2020-09-10
 */
@Service
public class EthLatestServiceImpl extends ServiceImpl<EthLatestMapper, EthLatest> implements IEthLatestService {

    @Autowired
    private IEthBlockService ethBlockService;

    @Autowired
    private EthSyncConfig    ethSyncConfig;

    @Override
    public Optional<EthLatest> getLatestBlock(String nodeName) {
        return Optional.ofNullable(
                lambdaQuery()
                .eq(EthLatest::getNodeName, nodeName)
                .one()
        );
    }

    @Override
    public void updateByHeight(long height) {
        EthBlock ethBlock = ethBlockService.getByHeight(height);
        lambdaUpdate().set(EthLatest::getHeight, height)
                .set(EthLatest::getHash, ethBlock.getBlockHash())
                .eq(EthLatest::getNodeName, ethSyncConfig.getNodeName())
                .update();
    }
}
