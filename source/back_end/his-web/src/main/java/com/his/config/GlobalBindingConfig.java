package com.his.config;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * 全局 String 入参归一：去首尾空白，空串按 null 处理（StringTrimmerEditor(true)）。
 *
 * <p>为什么要有：前端清空输入框/下拉传过来的是 ""，不归一的话空串会被当成有效值——
 * 「有值才更新」的判断失效、库里积 ""、唯一键把 "" 当成一行真实数据。
 *
 * <p>覆盖范围只有走 WebDataBinder 的入口：@RequestParam、@ModelAttribute（表单/查询对象）。
 * @RequestBody 的 JSON 走 Jackson，不受这里约束；@PathVariable 不经过 Binder。
 * 想让 JSON 也去空只能在 Jackson 侧配 StringDeserializer，别指望本类。
 */
@ControllerAdvice
public class GlobalBindingConfig {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }
}