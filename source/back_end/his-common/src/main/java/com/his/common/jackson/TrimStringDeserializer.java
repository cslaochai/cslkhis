package com.his.common.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;

import java.io.IOException;

/**
 * Jackson 反序列化器：对 String 字段去除前后空格。
 *
 * <p>用法（字段级精准，不影响其它字段与类型，天然早于 @Valid）：
 * <pre>
 *     &#64;JsonDeserialize(using = TrimStringDeserializer.class)
 *     private String username;
 * </pre>
 *
 * <p>委托 Jackson 自带的 {@link StringDeserializer} 完成基础反序列化后再 trim，
 * 能正确兼容 null、数字转字符串、unicode 转义等边界（比直接 p.getText() 更稳）。</p>
 *
 * <p><b>禁止标注</b>：密码、SM2 密文、以及需要保留前后/内部空格的精确匹配字段。</p>
 */
public class TrimStringDeserializer extends JsonDeserializer<String> {

    public static final TrimStringDeserializer INSTANCE = new TrimStringDeserializer();

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = StringDeserializer.instance.deserialize(p, ctxt);
        return value == null ? null : value.trim();
    }

    @Override
    public boolean isCachable() {
        return true;
    }
}
