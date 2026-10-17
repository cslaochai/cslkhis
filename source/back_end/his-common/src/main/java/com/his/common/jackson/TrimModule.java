package com.his.common.jackson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Jackson 全局模块：对所有 String 字段在反序列化阶段自动 trim，天然早于 @Valid。
 *
 * <p>仅对未显式标注 {@link JsonDeserialize} 的 String 字段生效（尊重自定义反序列化器），
 * 并按黑名单（password / smsCode / token / openid / 签名 / 密文 等）跳过敏感字段。</p>
 *
 * <p>覆盖全应用所有经 Jackson 反序列化的 String 字段（含嵌套 DTO），无需逐字段标注。
 * 注意：集合/数组内的 String 元素不在此处理（如需 trim 可显式标注或依赖 @AllowedValues(trim=true)）。</p>
 */
public class TrimModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    /** 黑名单：字段名（小写）包含以下任一子串的 String 字段不自动 trim。 */
    private static final Set<String> BLOCKLIST = new HashSet<>(Arrays.asList(
            "password", "pwd", "passwd",
            "smscode", "captcha", "verifycode", "verificationcode",
            "token", "accesstoken", "refreshtoken",
            "openid", "unionid", "appid",
            "cipher", "encrypt", "ciphertext", "encrypted",
            "signature", "signby",
            "secret", "salt", "nonce"
    ));

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.addBeanDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public BeanDeserializerBuilder updateBuilder(DeserializationConfig config,
                                                         BeanDescription beanDesc,
                                                         BeanDeserializerBuilder builder) {
                Map<String, SettableBeanProperty> props = new HashMap<>();
                Iterator<SettableBeanProperty> it = builder.getProperties();
                while (it.hasNext()) {
                    SettableBeanProperty p = it.next();
                    props.put(p.getName(), p);
                }
                beanDesc.findProperties().forEach(propDef -> {
                    AnnotatedField field = propDef.getField();
                    if (field == null) {
                        return;
                    }
                    // 已显式标注 @JsonDeserialize 的字段不覆盖，尊重自定义反序列化器
                    if (field.getAnnotation(JsonDeserialize.class) != null) {
                        return;
                    }
                    SettableBeanProperty target = props.get(propDef.getName());
                    if (target == null || target.getType().getRawClass() != String.class) {
                        return;
                    }
                    if (isBlocked(target.getName())) {
                        return;
                    }
                    builder.addOrReplaceProperty(
                            target.withValueDeserializer(TrimStringDeserializer.INSTANCE), true);
                });
                return builder;
            }
        });
    }

    private static boolean isBlocked(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        for (String b : BLOCKLIST) {
            if (lower.contains(b)) {
                return true;
            }
        }
        return false;
    }
}
