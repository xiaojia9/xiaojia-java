package com.tlias.common;

/**
 * 业务异常：由全局异常处理器统一转换为 Result.error(msg)
 */
public class BizException extends RuntimeException {

    public BizException(String message) {
        super(message);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }
}
