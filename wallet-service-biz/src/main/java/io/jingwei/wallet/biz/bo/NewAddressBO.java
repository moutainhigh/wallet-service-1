package io.jingwei.wallet.biz.bo;


import lombok.Data;
import lombok.experimental.Accessors;

/**
 *
 */
@Data
@Accessors(chain = true)
public class NewAddressBO  {
    /**
     * 地址
     */
    private String address;

}
