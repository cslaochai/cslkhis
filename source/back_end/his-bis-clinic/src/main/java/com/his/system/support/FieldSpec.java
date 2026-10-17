package com.his.system.support;

import com.his.system.enums.MaskEnum;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * 字段级审计/留痕的字段声明
 */
public record FieldSpec(String name, String label, MaskEnum maskEnum, Function<Object, String> renderer) {

    /**
     * 原样记录（mask 按字段名嗅探兜底）
     */
    public static FieldSpec of(String name, String label) {
        return new FieldSpec(name, label, null, null);
    }

    /**
     * 指定打码方式
     */
    public static FieldSpec masked(String name, String label, MaskEnum maskEnum) {
        return new FieldSpec(name, label, maskEnum, null);
    }

    /**
     * 码值渲染成人读文本（如 1 → 男）
     */
    public static FieldSpec render(String name, String label, Function<Object, String> renderer) {
        return new FieldSpec(name, label, null, renderer);
    }

    /**
     * 变长参数收成 List，业务侧声明常量用
     */
    public static List<FieldSpec> list(FieldSpec... specs) {
        return List.of(specs);
    }

    /**
     * 变长参数收成 List（Arrays.asList 版本，允许后续修改，一般不需要）
     */
    public static List<FieldSpec> listOf(FieldSpec... specs) {
        return Arrays.asList(specs);
    }

    /**
     * 按字段名兜底嗅探打码方式：新增字段忘了标 mask 时，靠名字也能挡住最要命的那几类。
     * （护照/军官证/医保卡号都落在 BANK_NO 这一档 —— 都是"前 4 后 4"的证件号口径。）
     */
    public static MaskEnum sniffMask(String fieldName) {
        String f = fieldName.toLowerCase();
        if (f.contains("idcard")) {
            return MaskEnum.ID_CARD;
        }
        if (f.contains("phone") || f.contains("mobile") || f.contains("tel")) {
            return MaskEnum.PHONE;
        }
        if (f.contains("bank") || f.contains("cardno") || f.contains("insuranceno")) {
            return MaskEnum.BANK_NO;
        }
        return MaskEnum.NONE;
    }

    /**
     * 实际生效的打码方式：显式优先，未指定则按字段名嗅探
     */
    public MaskEnum effectiveMask() {
        return maskEnum != null ? maskEnum : sniffMask(name);
    }
}
