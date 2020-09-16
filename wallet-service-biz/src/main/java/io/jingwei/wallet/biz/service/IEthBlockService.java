package io.jingwei.wallet.biz.service;

import io.jingwei.wallet.biz.entity.EthBlock;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Andy
 * @since 2020-06-02
 */
public interface IEthBlockService extends IService<EthBlock> {

    void saveBlock(org.web3j.protocol.core.methods.response.EthBlock.Block block);

    void deleteGTBlock(long blockHeight);

    EthBlock getByHash(String blockHash);

    EthBlock getByHeight(long height);

}
