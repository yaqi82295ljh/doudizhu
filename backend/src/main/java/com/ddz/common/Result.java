package com.ddz.common;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 统一响应体。所有 REST 接口一律返回该结构，便于前端统一处理。
 *
 * @param code    业务码，0 表示成功
 * @param message 提示信息
 * @param data    业务数据
 */
@Schema(name = "Result", description = "统一响应体")
public record Result<T>(
        @Schema(description = "业务码，0=成功", example = "0") int code,
        @Schema(description = "提示信息", example = "ok") String message,
        @Schema(description = "业务数据") T data
) {

    public static <T> Result<T> ok(T data) {
        return new Result<>(0, "ok", data);
    }

    public static Result<Void> ok() {
        return new Result<>(0, "ok", null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }
}
