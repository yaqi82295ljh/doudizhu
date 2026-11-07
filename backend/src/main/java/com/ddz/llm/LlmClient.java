package com.ddz.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * OpenAI 兼容协议的极简客户端，只做一件事：发一轮 messages，取回文本。
 * 任何失败（未配置 / 超时 / 非 200 / 解析不到 content）都返回 null，
 * 由调用方决定回退逻辑，这里不抛异常，避免拖垮对局线程。
 */
@Component
public class LlmClient {

    private static final Logger log = LoggerFactory.getLogger(LlmClient.class);

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(30);

    private final ObjectMapper mapper;
    private final HttpClient http;

    public LlmClient(ObjectMapper mapper) {
        this.mapper = mapper;
        this.http = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
    }

    public String chat(LlmConfig cfg, String systemPrompt, String userPrompt, int maxTokens) {
        if (cfg == null || !cfg.usable()) {
            return null;
        }

        try {
            String json = mapper.writeValueAsString(Map.of(
                    "model", cfg.model(),
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)),
                    "temperature", 0.2,
                    "max_tokens", maxTokens));

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint(cfg.baseUrl())))
                    .timeout(READ_TIMEOUT)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + cfg.apiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            long start = System.currentTimeMillis();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            long cost = System.currentTimeMillis() - start;

            if (resp.statusCode() != 200) {
                log.warn("LLM 调用失败 status={} cost={}ms key={} body={}",
                        resp.statusCode(), cost, cfg.maskedKey(), brief(resp.body()));
                return null;
            }

            JsonNode content = mapper.readTree(resp.body())
                    .path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.isNull()) {
                log.warn("LLM 响应缺少 choices[0].message.content，body={}", brief(resp.body()));
                return null;
            }

            log.debug("LLM 调用成功 cost={}ms model={}", cost, cfg.model());
            return content.asText();

        } catch (Exception e) {
            log.warn("LLM 调用异常 key={} : {}", cfg.maskedKey(), e.toString());
            return null;
        }
    }

    /** baseUrl 允许填到 /v1 或只填域名，这里统一补全成 .../chat/completions */
    static String endpoint(String baseUrl) {
        String base = baseUrl.trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base.endsWith("/chat/completions") ? base : base + "/chat/completions";
    }

    private static String brief(String body) {
        if (body == null) {
            return "";
        }
        return body.length() <= 200 ? body : body.substring(0, 200) + "...";
    }
}
