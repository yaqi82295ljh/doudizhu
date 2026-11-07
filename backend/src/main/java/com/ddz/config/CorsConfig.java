package com.ddz.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 开发期跨域配置。
 * <p>
 * 前端 Vite 开发服务器（localhost:5173 / 127.0.0.1:5173）访问后端 8080 属跨域，这里放行本机所有端口。
 * 生产环境前端由 Nginx 同源托管，CORS 不生效也不影响。
 * <p>
 * 注意：用 {@code allowedOriginPatterns} 而非 {@code allowedOrigins("*")}——
 * 后者与 {@code allowCredentials(true)} 互斥，Spring 会直接抛异常。
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
