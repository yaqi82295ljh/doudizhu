package com.ddz.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 接口文档配置（springdoc 3.x + Swagger UI）。
 * <p>
 * 访问入口：
 * <ul>
 *   <li>Swagger UI：{@code http://localhost:8080/swagger-ui.html}</li>
 *   <li>OpenAPI JSON：{@code http://localhost:8080/v3/api-docs}</li>
 * </ul>
 * 不选 Knife4j 的原因见立项书 ADR-06。
 * <p>
 * WebSocket 事件契约（立项书 §5.2）无法用 OpenAPI 表达，仍以文档形式维护。
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI ddzOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("斗地主 Web API")
                        .version("v1")
                        .description("""
                                斗地主服务端 REST 接口文档。
                                - `/api/ping`、`/api/auth/**` 无需鉴权
                                - 其余接口需请求头 `Authorization: Bearer <accessToken>`（ADR-05）
                                - 实时对局走 WebSocket `/ws/game`，事件契约见 docs/立项书.md §5.2
                                """)
                        .contact(new Contact().name("doudizhu")))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Access Token，有效期 2 小时")))
                // 全局登记一次，UI 上每个接口都带小锁图标，可直接填 token 调试
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
