package com.his.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.his.ai.entity.SysAiPromptTemplate;
import com.his.ai.mapper.SysAiPromptTemplateMapper;
import com.his.ai.support.PromptTemplate;
import com.his.common.base.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai/promptTemplate")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AiPromptTemplateController {

    private final SysAiPromptTemplateMapper promptTemplateMapper;
    private final PromptTemplate promptTemplate;

    @GetMapping("/listPage")
    @PreAuthorize("hasAuthority('ai:operationQa:config')")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<SysAiPromptTemplate>> listPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysAiPromptTemplate> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysAiPromptTemplate> wrapper = new LambdaQueryWrapper<SysAiPromptTemplate>()
                .eq(SysAiPromptTemplate::getIsActive, 1)
                .orderByAsc(SysAiPromptTemplate::getTemplateCode);
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(SysAiPromptTemplate::getTemplateCode, kw)
                    .or().like(SysAiPromptTemplate::getTemplateName, kw));
        }
        return Result.success(promptTemplateMapper.selectPage(page, wrapper));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAuthority('ai:operationQa:config')")
    public Result<List<SysAiPromptTemplate>> history(@RequestParam String code) {
        List<SysAiPromptTemplate> list = promptTemplateMapper.selectList(
                new LambdaQueryWrapper<SysAiPromptTemplate>()
                        .eq(SysAiPromptTemplate::getTemplateCode, code)
                        .orderByDesc(SysAiPromptTemplate::getIsActive)
                        .orderByDesc(SysAiPromptTemplate::getId));
        return Result.success(list);
    }

    @PostMapping("/upsert")
    @PreAuthorize("hasAuthority('ai:operationQa:config')")
    public Result<String> upsert(@RequestBody SysAiPromptTemplate body) {
        String code = body.getTemplateCode();
        String content = body.getContent();
        String remark = body.getRemark();
        if (code == null || content == null) {
            throw new IllegalArgumentException("模板编码和内容不能为空");
        }
        // 把当前激活的设为停用
        promptTemplateMapper.update(null,
                new LambdaUpdateWrapper<SysAiPromptTemplate>()
                        .eq(SysAiPromptTemplate::getTemplateCode, code)
                        .eq(SysAiPromptTemplate::getIsActive, 1)
                        .set(SysAiPromptTemplate::getIsActive, 0));
        // 新版本号
        SysAiPromptTemplate latest = promptTemplateMapper.selectOne(
                new LambdaQueryWrapper<SysAiPromptTemplate>()
                        .eq(SysAiPromptTemplate::getTemplateCode, code)
                        .orderByDesc(SysAiPromptTemplate::getId)
                        .last("LIMIT 1"));
        String newVersion = "1.0";
        if (latest != null && latest.getVersion() != null) {
            try {
                double v = Double.parseDouble(latest.getVersion());
                newVersion = String.format("%.1f", v + 0.1);
            } catch (NumberFormatException ignored) {}
        }
        SysAiPromptTemplate entity = new SysAiPromptTemplate();
        entity.setTemplateCode(code);
        entity.setTemplateName(latest != null ? latest.getTemplateName() : code);
        entity.setVersion(newVersion);
        entity.setContent(content);
        entity.setIsActive(1);
        entity.setRemark(remark);
        promptTemplateMapper.insert(entity);
        promptTemplate.evictCache(code);
        return Result.success("v" + newVersion);
    }

    /** 版本回退：把指定历史版本切为生效版 */
    @PostMapping("/activate")
    @PreAuthorize("hasAuthority('ai:operationQa:config')")
    public Result<String> activate(@RequestParam Long id) {
        SysAiPromptTemplate row = promptTemplateMapper.selectById(id);
        if (row == null) {
            throw new IllegalArgumentException("版本不存在");
        }
        String code = row.getTemplateCode();
        // 全部设为停用
        promptTemplateMapper.update(null,
                new LambdaUpdateWrapper<SysAiPromptTemplate>()
                        .eq(SysAiPromptTemplate::getTemplateCode, code)
                        .set(SysAiPromptTemplate::getIsActive, 0));
        // 目标版本设为生效
        row.setIsActive(1);
        promptTemplateMapper.updateById(row);
        promptTemplate.evictCache(code);
        return Result.success("已切回 v" + row.getVersion());
    }
}
