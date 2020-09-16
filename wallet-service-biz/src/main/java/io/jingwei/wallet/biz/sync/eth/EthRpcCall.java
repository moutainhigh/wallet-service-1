package io.jingwei.wallet.biz.sync.eth;

import io.jingwei.base.utils.exception.BizErr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import static io.jingwei.wallet.biz.exception.WalletErrCode.*;

/**
 *
 */
@Slf4j
@Component
public class EthRpcCall {

    @Autowired
    private Web3jClient web3jClient;

    /**
     * 单位是最小单位信息
     * @param address
     * @return
     * @throws Exception
     */
    public BigInteger getAddressBalance(Web3j web3j,String address) {
        try {
            EthGetBalance ethGetBalance = web3j
                    .ethGetBalance(address, DefaultBlockParameterName.LATEST)
                    .sendAsync()
                    .get();
            BigInteger wei = ethGetBalance.getBalance();
            return wei;
        } catch (Exception ex) {
            log.error("getAddressBalance, ex={}", ex);
            throw new BizErr(GET_BALANCE_ERR);
        }
    }

    public String getBlockHash(String parentHash) {
        Web3j web3j = web3jClient.getWeb3j();
        try {
            return web3j.ethGetBlockByHash(parentHash, false)
                    .send()
                    .getBlock()
                    .getParentHash();
        } catch (IOException e) {
            e.printStackTrace();
            throw new BizErr(GET_BLOCK_PHASH_ERR);
        }
    }

    /**
     * 获取矿工费用
     * @return
     * @throws IOException
     */
    public BigInteger getGasPrice(Web3j web3j)  {
        try {
            EthGasPrice ethGasPrice = web3j.ethGasPrice().send();
            return ethGasPrice.getGasPrice();
        } catch (Exception ex) {
            log.error("getGasPrice, ex={}", ex);
            throw new BizErr(GET_GAS_PRICE_ERR);
        }

    }

    public EthBlock.Block getBlockByNumber(Web3j web3j, long blockNum) {
        EthBlock ethBlock;
        try {
            ethBlock = web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNum)),
                    true).send();
        } catch (IOException e) {
            throw new BizErr(GET_BLOCK__ERR);
        }
        return ethBlock.getBlock();
    }

    /**
     * 获取区块的高度信息
     *
     * @return
     * @throws IOException
     */
    public long getBlockHeight() {
        Web3j web3j = web3jClient.getWeb3j();
        try {
            EthBlockNumber ethBlockNumber = web3j.ethBlockNumber().send();
            return ethBlockNumber.getBlockNumber().longValue();
        } catch (Exception ex) {
            log.error("getBlockHeight, ex={}", ex);
            throw new BizErr(GET_BLOCK_HEIGHT_ERR);
        }

    }




    /**
     * 估计的gasused
     *
     * @param web3j
     * @param transaction
     * @return
     */
    public BigInteger getTransactionGasLimit(Web3j web3j,org.web3j.protocol.core.methods.request.Transaction transaction) {
        BigInteger gasLimit = BigInteger.ZERO;
        try {
            EthEstimateGas ethEstimateGas = web3j.ethEstimateGas(transaction).send();
            gasLimit = ethEstimateGas.getAmountUsed();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gasLimit;
    }


    /**
     * 获取地址的交易数信息
     *
     * @param web3j
     * @param address
     * @return
     * @throws Exception
     */
    public BigInteger getNonce(Web3j web3j,String address)  {
        return getNonce(web3j,address,DefaultBlockParameterName.LATEST);
    }

    /**
     * 获取地址的交易数信息
     *
     * @param web3j
     * @param address
     * @return
     * @throws Exception
     */
    public BigInteger getNonce(Web3j web3j,String address,DefaultBlockParameterName defaultBlockParameterName){
        try {
            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                    address, defaultBlockParameterName).sendAsync().get();

            return ethGetTransactionCount.getTransactionCount();
        } catch (Exception ex) {
            log.error("getNonce, ex={}", ex);
            throw new BizErr(GET_NONCE_ERR);
        }


    }


    /**
     * 获取交易的详细信息
     *
     * @param transactionHash
     * @return
     * @throws Exception
     */
    public Optional<TransactionReceipt> getReceipt(String transactionHash) {
        Web3j web3j = web3jClient.getWeb3j();
        EthGetTransactionReceipt receipt = null;
        try {
            receipt = web3j
                    .ethGetTransactionReceipt(transactionHash)
                    .sendAsync()
                    .get();
        } catch (Exception e) {
           return Optional.empty();
        }

        return receipt.getTransactionReceipt();
    }


    /**
     * 获取交易的详细信息
     *
     * @param web3j
     * @param transactionHash
     * @return
     * @throws Exception
     */
    public Optional<Transaction> getTranscation(Web3j web3j, String transactionHash)
            throws Exception {
        EthTransaction receipt = web3j
                .ethGetTransactionByHash(transactionHash)
                .sendAsync()
                .get();
        return receipt.getTransaction();
    }

    /**
     * @param web3j
     * @param fromAddress
     * @param toAddress
     * @param amount
     * @return
     */
    public  String createTransaction(Web3j web3j, String fromAddress, String toAddress, BigInteger amount, BigInteger gasPrice,BigInteger gasLimit) {
        String hexValue = null;
        try {
            BigInteger nonce = getNonce(web3j, fromAddress);
            if (gasPrice == null) {
                //默认用以太坊网络的gas
                gasPrice = getGasPrice(web3j);
            }
            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, toAddress,amount);
            byte[] bytes = TransactionEncoder.encode(rawTransaction);
            return Numeric.toHexString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hexValue;
    }

    /**
     * @param web3j
     * @param fromAddress
     * @param toAddress
     * @param amount
     * @return
     */
    public RawTransaction createRawTransaction(Web3j web3j, String fromAddress, String toAddress, BigInteger amount, BigInteger gasPrice,BigInteger gasLimit) {
        RawTransaction rawTransaction = null;
        try {
            BigInteger nonce = getNonce(web3j, fromAddress,DefaultBlockParameterName.PENDING);
            if (gasPrice == null) {
                //默认用以太坊网络的gas
                gasPrice = getGasPrice(web3j);
            }
            rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, toAddress,amount);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rawTransaction;
    }



    public static BigDecimal weiToEther(BigInteger wei) {
        return Convert.fromWei(wei.toString(), Convert.Unit.ETHER);
    }

    public static BigInteger etherToWei(BigDecimal ether) {
        return Convert.toWei(ether, Convert.Unit.ETHER).toBigInteger();
    }


}
