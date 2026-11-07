package com.ddz.llm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 单机版 LLM 配置容器：一个进程一份，前端通过 REST 覆盖。
 *
 * 初始值来自 application.yml 的 ddz.llm.*，前端设置后覆盖。
 * 重启即失效——这是有意为之，key 不落盘，避免任何形式的持久化泄露。
 * volatile：Controller 线程写，对局线程读。
 */
@Component
public class LlmConfigStore {

    private volatile LlmConfig current;

    public LlmConfigStore(
            @Value("${ddz.llm.base-url:}") String baseUrl,
            @Value("${ddz.llm.api-key:}") String apiKey,
            @Value("${ddz.llm.model:deepseek-chat}") String model) {
        this.current = new LlmConfig(baseUrl, apiKey, model);
    }


    public LlmConfig get() {
        return current;
    }

    public void update(LlmConfig cfg) {
        this.current = cfg;
    }

    /** 是否具备调用条件（三个字段齐全） */
    public boolean ready() {
        return current != null && current.usable();
    }
}
