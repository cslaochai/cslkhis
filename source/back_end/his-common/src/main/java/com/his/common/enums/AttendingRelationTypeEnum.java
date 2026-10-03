package com.his.common.enums;

import lombok.Getter;

/**
 * 住院管床关系类型枚举（sql/202，字典 {@code his_attending_relation_type}）
 *
 * <p>真实医院里一个患者身上同时挂着好几层医生关系，混成一个「主管」字段会丢信息：
 * <ul>
 *   <li><b>主管（管床）</b>：日常负责，写病历、开医嘱、跟家属谈话，一个患者只有一个；</li>
 *   <li><b>主诊组长</b>：对这一组的医疗质量负责，不一定是管床那个人；</li>
 *   <li><b>协作</b>：会诊后参与治疗的其他医生，可以有多个。</li>
 * </ul>
 * 唯一键 {@code (admission_id, relation_type, employee_id)} 用的是「一次住院 × 一个类型 × 一个医生」：
 * 主管天然唯一（一个患者只有一个主管），协作允许多人。
 */
@Getter
public enum AttendingRelationTypeEnum {

    /** 主管（管床医生） */
    ATTENDING(1, "主管"),
    /** 主诊组长 */
    GROUP_LEADER(2, "主诊组长"),
    /** 协作（会诊/参与治疗） */
    ASSIST(3, "协作");

    private final int code;
    private final String label;

    AttendingRelationTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AttendingRelationTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AttendingRelationTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }

    public static String labelOf(Integer code) {
        AttendingRelationTypeEnum type = fromCode(code);
        return type == null ? "未知(" + code + ")" : type.getLabel();
    }

    /**
     * 该类型在一次住院下是否只允许一人。主管与主诊组长是「谁负责」的唯一答案，
     * 协作不是 —— 一次全院会诊可能有好几个协作医生。
     */
    public static boolean exclusive(Integer code) {
        return ATTENDING.code == code || GROUP_LEADER.code == code;
    }

    public static String whitelistText() {
        StringBuilder sb = new StringBuilder();
        for (AttendingRelationTypeEnum type : values()) {
            sb.append(sb.length() == 0 ? "" : " / ").append(type.code).append("-").append(type.label);
        }
        return sb.toString();
    }
}
