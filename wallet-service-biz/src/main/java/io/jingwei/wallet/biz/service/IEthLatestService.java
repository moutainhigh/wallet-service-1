package io.jingwei.wallet.biz.service;

import io.jingwei.wallet.biz.entity.EthLatest;
import com.baomidou.mybatisplus.extension.service.IService;

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
     * @param nodeName The nodename
     * @return The block details
     */
    Optional<EthLatest> getLatestBlock(String nodeName);

    void updateByHeight(long height);
}
