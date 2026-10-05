package com.his.system.support;

import com.his.system.enums.MaskEnum;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * 字段级留痕的字段声明：要记哪个字段、中文名叫啥、值怎么打码、码值怎么翻成人话。
 *
 * <p><b>为什么要声明而不是直接反射全字段：</b>反射全字段会把 password、updateTime、
 * delFlag 这些也记进来，日志一半是噪音，还可能把口令原文写进审计表 —— 那是事故。
 * 显式声明一遍，顺手就把"这个对象有哪些字段值得被审计"这件事钉死在代码里。
 *
 * <p>四种写法（业务侧按需挑）：
 * <pre>{@code
 *   of("patientName", "姓名")                       // 原样记
 *   masked("idCard", "身份证号", Mask.ID_CARD)       // 打码后记
 *   render("gender", "性别", v -> SysGenderEnum.getText((Integer) v))     // 1/2/9 → 男/女/未知，翻成人话再记
 *   render("status", "状态", v -> statusText(v), Mask.NONE)
 * }</pre>
 *
 * <p>不给 mask 时会按字段名兜底嗅探（见 {@code FieldChangeRecorder#sniffMask}）：
 * 名字里带 idCard / phone / bank 的自动打码 —— 新增字段忘了标 mask 也不至于裸奔。
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
     * 实际生效的打码方式：显式优先，未指定则按字段名嗅探
     */
    MaskEnum effectiveMask() {
        return maskEnum != null ? maskEnum : FieldChangeRecorder.sniffMask(name);
    }
}
