package io.jingwei.wallet.biz.exception;


import io.jingwei.base.utils.exception.IBizErrCode;

public enum WalletErrCode implements IBizErrCode {
    GET_BALANCE_ERR("12400", "get-balance-error"),
    GET_GAS_PRICE_ERR("12401", "get-gas-price-error"),
    GET_BLOCK_HEIGHT_ERR("12402", "get-block-height-error"),
    GET_NONCE_ERR("12403", "get-nonce-error"),
    GET_BLOCK__ERR("12404", "get-block-error"),
    GET_RECEIPT_ERR("12405", "get-receipt-error"),
    GET_BLOCK_PHASH_ERR("12406", "get-parent-hash-error"),
    MERCHANT_DISABLED("12305", "merchant-disabled"),
    MERCHANT_NOT_INIT("12306", "merchant-not-init"),
    SECRET_KEY_EXPIRED("12307", "secret-key-expired"),
    LIMIT_STOP_ORDER_PRICE_INVALID("12007", "limit-stop-order-price-invalid"),
    TRIGGER_PRICE_INVALID("12008", "trigger-price-invalid"),
    ORDER_VOLUME_INVALID("12009", "order-volume-invalid"),
    ORDER_TYPE_NOT_SUPPORT("12010", "order-type-not-support"),
    PLACE_ORDER_FAILED("12015", "place-order-failed")
    ;

    /**
     * 枚举编码
     */
    private String code;

    /**
     * 描述说明
     */
    private String desc;

    WalletErrCode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }


    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String getMsg() {
        return getClass().getName() + '.' + name();
    }

}
