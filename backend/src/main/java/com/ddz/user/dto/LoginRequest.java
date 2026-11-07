package com.ddz.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 登录 / 注册请求体。M1 阶段接入；现在先固化契约，前端可据此先行开发。
 */
@Schema(name = "LoginRequest", description = "登录或注册请求")
public record LoginRequest(

        @Schema(description = "登录名，3~32 位", example = "alice", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "不能为空")
        @Size(min = 3, max = 32, message = "长度需为 3~32 位")
        String username,

        @Schema(description = "明文密码，6~64 位，服务端用 BCrypt 哈希后入库",
                example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "不能为空")
        @Size(min = 6, max = 64, message = "长度需为 6~64 位")
        String password
) {
}
