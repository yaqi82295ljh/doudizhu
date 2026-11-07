package com.ddz.common;

/**
 * 业务异常。抛出后由 {@link GlobalExceptionHandler} 转成 {@link Result}。
 * 与系统异常区分开：业务异常是"可预期的失败"（如用户名已存在），不打 error 日志。
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        this(400, message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
