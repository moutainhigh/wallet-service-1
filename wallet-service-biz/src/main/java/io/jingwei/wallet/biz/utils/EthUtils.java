package io.jingwei.wallet.biz.utils;

import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;

public class EthUtils {
    public static BigDecimal fromWei(BigInteger  value){
        return Convert.fromWei(new BigDecimal(value), Convert.Unit.ETHER).stripTrailingZeros();
    }
}
