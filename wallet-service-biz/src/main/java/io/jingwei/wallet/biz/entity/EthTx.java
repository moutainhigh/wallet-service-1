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
 * @since 2020-06-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class EthTx implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String txHash;

    private Long blockHeight;

    private Long blockTime;

    private String fromAddress;

    private String toAddress;

    /**
     * 交易索引号
     */
    private Integer txIndex;

    private BigDecimal amount;

    private Long feePrice;

    private Long feeUsed;

    private Boolean status;

    private Boolean processed;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
