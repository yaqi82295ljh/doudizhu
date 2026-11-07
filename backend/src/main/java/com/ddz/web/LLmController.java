package com.ddz.web;


import com.ddz.common.Result;
import com.ddz.llm.LlmConfig;
import com.ddz.llm.LlmConfigStore;
import com.ddz.llm.dto.LlmConfigDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "LLM模型接入", description = "")
@RestController
@RequestMapping("api/llm")
public class LLmController {

    /*
    * { "baseUrl": "https://api.deepseek.com", "model": "deepseek-flash",
        "keyMasked": "sk-1****09d1", "ready": true }
*/

    @PostMapping("/config")
    public Result updateConfig( @RequestBody LlmConfigDTO dto) {
        String baseUrl = dto.baseUrl;
        String model = dto.model;
        boolean ready = dto.ready;
        String keyMasked = dto.key;
        LlmConfig llmConfig = new LlmConfig(baseUrl,keyMasked,model);
        LlmConfigStore.update(llmConfig);
        return Result.ok();
    }

    @GetMapping("/config")
    public Result getConfig(){

        return Result.ok();
    }


    public static String mask(String s, int prefix, int suffix, int stars) {
        if (s == null || s.isEmpty()){
            return s;
        }
        if (s.length() <= prefix + suffix) {
            return "*".repeat(s.length());
        }
        return s.substring(0, prefix)
                + "*".repeat(stars)
                + s.substring(s.length() - suffix);
    }


}
