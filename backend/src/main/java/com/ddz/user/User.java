package com.ddz.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体，对应表 {@code user}。
 * <p>
 * {@code passwordHash} 只用于服务端校验，任何情况下不得出现在响应体里——对外一律走 DTO。
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** BCrypt 哈希，长度 60 */
    private String passwordHash;

    private String nickname;

    private String avatar;

    /** 软删除标记：0=正常 1=已删除（配合 application.yml 的 logic-delete-field） */
    @TableLogic
    private Integer deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
