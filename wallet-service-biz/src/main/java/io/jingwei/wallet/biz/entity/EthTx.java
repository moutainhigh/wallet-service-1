package io.jingwei.wallet.biz.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author Andy
 * @since 2020-09-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class EthTx implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 交易hash
     */
    private String txHash;

    /**
     * 区块高度
     */
    private Long blockHeight;

    /**
     * 区块时间
     */
    private Long blockTime;

    /**
     * 交易转出地址
     */
    private String fromAddress;

    /**
     * 交易转入地址
     */
    private String toAddress;

    /**
     * 交易索引号
     */
    private Integer txIndex;

    /**
     * 交易量
     */
    private BigDecimal amount;

    /**
     * 交易手续费价格
     */
    private Long feePrice;

    /**
     * 交易手续费数量
     */
    private Long feeUsed;

    /**
     * 交易状态
     */
    private Boolean success;

    /**
     * 交易是否被通知
     */
    private Boolean notified;

    /**
     * 交易是否被删除
     */
    private Boolean deleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
