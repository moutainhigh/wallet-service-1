package io.jingwei.wallet.biz.service;

import io.jingwei.wallet.biz.entity.EthLatest;
import com.baomidou.mybatisplus.extension.service.IService;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.util.Optional;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Andy
 * @since 2020-09-10
 */
public interface IEthLatestService extends IService<EthLatest> {
    /**
     * Returns the latest block, for the specified node.
     *
     * @return The block details
     */
    Optional<EthLatest> getLatestBlock();

    void updateByOldBlock(long height);

    void updateByNewBlock(EthBlock.Block block);
}
