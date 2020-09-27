package io.jingwei.wallet.biz.service.impl;

import io.jingwei.wallet.biz.bo.NewAddressBO;
import io.jingwei.wallet.biz.entity.EthAddress;
import io.jingwei.wallet.biz.exception.CryptException;
import io.jingwei.wallet.biz.service.IEthAddressService;
import io.jingwei.wallet.biz.service.IWalletService;
import io.jingwei.wallet.biz.utils.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

@Slf4j
@Service
public class WalletServiceImpl implements IWalletService {
    @Value("${eth.encrypt.key:FIN-WALLET}")
    private String                            encryptKey;

    @Autowired
    private IEthAddressService                ethAddressService;

    @Override
    public NewAddressBO getNewEthAddress(String bizType) {
        ECKeyPair ecKeyPair;
        try {
            ecKeyPair = Keys.createEcKeyPair();
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new CryptException("Generate new eth address failed", e);
        }

        String publicKey = Numeric.toHexStringWithPrefix(ecKeyPair.getPublicKey());
        String privateKey = Numeric.toHexStringNoPrefix(ecKeyPair.getPrivateKey());
        String encryptedPrivateKey = AesUtil.encryptToString(privateKey, encryptKey);
        String address = "0x" + Keys.getAddress(publicKey);

        ethAddressService.save(new EthAddress()
                .setAddress(address)
                .setBizType(bizType)
                .setPublicKey(publicKey)
                .setPrivateKey(encryptedPrivateKey)
                .setStatus(true));

        return new NewAddressBO().setAddress(address);
    }
}
