package io.jingwei.wallet.biz.service;

import io.jingwei.wallet.biz.bo.NewAddressBO;

public interface IWalletService {
    NewAddressBO getNewEthAddress(String bizType);
}
