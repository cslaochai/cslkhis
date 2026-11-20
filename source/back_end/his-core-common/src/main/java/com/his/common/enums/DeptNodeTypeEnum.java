package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 科室节点类型（sys_department.node_type）。
 *
 * <p>与 {@link DeptTypeEnum}（dept_type=服务范围：门诊/住院/急诊/医技…）是两个正交维度：
 * 本枚举回答「这个节点在组织树上是什么角色」，dept_type 回答「它对患者提供哪些服务」。
 * 两者混用过的后果：管理片区（内科系统）因为挂着「住院科室」被当成可收治、可排班的科室。
 *
 * <p>节点角色是结构性事实（后端拿它做过滤与校验），因此落枚举不落字典；
 * 服务范围仍由字典 his_dept_type 驱动（操作员可维护）。改码值必须同步 docs 增量 SQL 与列注释。
 */
@Getter
public enum DeptNodeTypeEnum {

    /** 医疗机构本身：科室树的根节点（本系统只有一个院区，不再单独建院区层） */
    ROOT("0", "医疗机构"),
    /** 管理片区/系统：只做组织归集与审核权落点（科护士长、护理审核人），不是可排班、可收治的科室 */
    AREA("1", "管理片区"),
    /** 临床科室：真正收治患者、排医生/护士班的末级科室 */
    CLINICAL("2", "临床科室"),
    /** 医技科室：检验/放射/超声/病理等，有值班排班但无床位 */
    TECH("3", "医技科室"),
    /** 药剂科室：药库/药房，有排班无床位 */
    PHARMACY("4", "药剂科室"),
    /** 行政后勤：职能部门，不参与临床排班与号源 */
    ADMIN("5", "行政后勤"),
    /** 医疗辅助：消毒供应/病案/设备等支撑科室 */
    SUPPORT("6", "医疗辅助"),
    /** 其他：体检中心、科研教学、虚拟科室等 */
    OTHER("7", "其他"),
    ;

    private final String code;
    private final String label;

    DeptNodeTypeEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DeptNodeTypeEnum fromCode(String code) {
        if (null == code) {
            return null;
        }
        for (DeptNodeTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    /** 是否管理片区：片区只能作为组织归集与审核权落点，不能当排班单元/收治科室 */
    public static boolean isArea(String code) {
        return AREA.code.equals(code);
    }

    /**
     * 是否可作为「业务科室」被排班/开诊/收治：临床、医技、药剂、医疗辅助、其他。
     * 片区与行政后勤返回 false —— 这两类节点没有任何人真正在那里上班。
     */
    public static boolean isBusinessDept(String code) {
        return CLINICAL.code.equals(code) || TECH.code.equals(code) || PHARMACY.code.equals(code)
                || SUPPORT.code.equals(code) || OTHER.code.equals(code);
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(String code) {
        DeptNodeTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(String code, String defaultValue) {
        DeptNodeTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(String code) {
        DeptNodeTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
