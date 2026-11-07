package com.ddz.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 登录响应体。刻意不含 {@code passwordHash}——对外 DTO 与实体分离是硬约定。
 */
@Schema(name = "LoginResponse", description = "登录成功返回")
public record LoginResponse(

        @Schema(description = "JWT Access Token，有效期 2 小时")
        String accessToken,

        @Schema(description = "Access Token 剩余有效秒数", example = "7200")
        long expiresIn,

        @Schema(description = "当前用户信息")
        UserInfo user
) {

    @Schema(name = "UserInfo", description = "用户公开信息")
    public record UserInfo(
            @Schema(description = "用户 id", example = "1") Long id,
            @Schema(description = "登录名", example = "alice") String username,
            @Schema(description = "昵称", example = "爱丽丝") String nickname,
            @Schema(description = "头像 URL") String avatar
    ) {
    }
}
