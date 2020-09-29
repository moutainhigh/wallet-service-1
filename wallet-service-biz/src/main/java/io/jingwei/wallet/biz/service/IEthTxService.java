package io.jingwei.wallet.biz.service;

import io.jingwei.wallet.biz.entity.EthTx;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Andy
 * @since 2020-06-02
 */
public interface IEthTxService extends IService<EthTx> {

    void saveOrUpdateList(Collection<EthTx> entityList);

    List<EthTx> listConfirmedTx(long currentHeight);

    List<EthTx> listUnconfirmedTx(long currentHeight);

    boolean updateConfirmNotified(String txHash);

    boolean updateUnconfirmNotified(String txHash);

    Optional<EthTx> getByHash(String txHash);
}
