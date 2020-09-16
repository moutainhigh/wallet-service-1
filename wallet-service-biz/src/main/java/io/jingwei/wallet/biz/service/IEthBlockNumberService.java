
package io.jingwei.wallet.biz.service;


import io.jingwei.wallet.biz.entity.EthLatest;

import java.util.Optional;

public interface IEthBlockNumberService {

    long getStartBlockForNode();

    Optional<EthLatest> getLatestBlock();
}
