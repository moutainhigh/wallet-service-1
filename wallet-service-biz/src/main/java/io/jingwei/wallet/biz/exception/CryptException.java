package io.jingwei.wallet.biz.exception;


public class CryptException extends RuntimeException {

    public CryptException() {
        super();
    }

    public CryptException(String message) {
        super(message);
    }

    public CryptException(String message, Throwable cause) {
        super(message, cause);
    }

    public CryptException(Throwable cause) {
        super(cause);
    }


}
