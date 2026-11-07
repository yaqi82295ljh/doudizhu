package com.ddz.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper。
 * <p>
 * 只用 {@link BaseMapper} 提供的单表 CRUD；复杂查询写在 XML 里，不在 Service 层拼字符串。
 * 所有查询条件都走参数绑定，不存在 SQL 注入面。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
