package com.his.common.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 排班域（biz_schedule 一张表按域拆页后的视图口径）。
 *
 * <p>背景：biz_schedule 同时承载四类排班事实，页面按域拆分后各自只看自己域的行：
 * <ul>
 *   <li>{@link #STAFF} 岗位出勤 —— 「员工排班」页（行政/总值班/医技/药学/收费等非临床专页域）；</li>
 *   <li>{@link #ER} 急诊值班 —— 「急诊排班」页（急诊科室 + 医生岗，值班公开表的数据源）。</li>
 * </ul>
 * 门诊号源（clinic_flag=1）归门诊排班页、护理归护理排班页，都不在本枚举内 ——
 * 那两个域有自己的页面与审批链，员工排班侧对它们只做「排除」不做「承载」。
 *
 * <p>为什么用域而不是各页自己拼过滤条件：三个页面共用同一套 upsert/listPage 接口，
 * 域码值是服务端唯一的过滤与拦截依据 —— 前端传错域看到的就不是自己域的行，
 * 比报错更危险；且保存侧的越域拦截（员工排班页排医生）必须服务端兜底，
 * 光靠前端藏选项挡不住直调。
 */
@Getter
public enum StaffScheduleScopeEnum {

    /**
     * 岗位出勤域：非急诊科室 + 非医生/护理岗 + 非出诊放号行。
     */
    STAFF(1, "岗位出勤"),

    /**
     * 急诊值班域：急诊科室（dept_type=6）+ 医生岗。
     */
    ER(2, "急诊值班"),
    ;

    private final Integer code;
    private final String label;

    StaffScheduleScopeEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    public static StaffScheduleScopeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (StaffScheduleScopeEnum e : values()) {
            if (Objects.equals(e.code, code)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 码值 → 文案；未知码值原样返回（未知[x] 是 bug 的线索，假数据不是）。
     */
    public static String labelOf(Integer code) {
        StaffScheduleScopeEnum e = fromCode(code);
        return e == null ? String.valueOf(code) : e.label;
    }
}
