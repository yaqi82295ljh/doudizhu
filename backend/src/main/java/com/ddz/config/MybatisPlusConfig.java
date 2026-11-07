package com.ddz.config;

import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置。
 * <p>
 * M0 阶段不放任何插件：Mapper 接口用 {@code @Mapper} 逐个标注，不依赖 MapperScan。
 * <p>
 * <b>M1 需要分页时再补两步</b>（现在加会引入无用依赖）：
 * <ol>
 *   <li>pom 增加 {@code com.baomidou:mybatis-plus-jsqlparser}（MP 3.5.9 起把 JSqlParser 拆成独立坐标，
 *       不加它注册分页插件会在运行期报 {@code ClassNotFoundException}）；</li>
 *   <li>在此处注册 {@code MybatisPlusInterceptor} + {@code PaginationInnerInterceptor(DbType.MYSQL)}。</li>
 * </ol>
 */
@Configuration
public class MybatisPlusConfig {
}
