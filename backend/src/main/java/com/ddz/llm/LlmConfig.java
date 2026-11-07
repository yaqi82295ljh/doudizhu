package com.ddz.llm;

/**
 * 前端可覆盖的 LLM 连接配置。
 * baseUrl 允许两种填法：
 *   https://api.deepseek.com
 *   https://dashscope.aliyuncs.com/compatible-mode/v1
 */
public record LlmConfig(String baseUrl, String apiKey, String model) {

    public boolean usable() {
        return notBlank(baseUrl) && notBlank(apiKey) && notBlank(model);
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    /** 打日志用：只留前 4 位和后 4 位，绝不输出完整 key */
    public String maskedKey() {
        if (apiKey == null || apiKey.length() < 12) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
