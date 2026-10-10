package com.his.medicaltech.support;

import com.his.patient.enums.AgeUnitEnum;

import java.util.List;

/**
 * 分组事实（一次入组所需的全部病案维度，编码已归一）。
 *
 * <p>字段与官方规则 DSL 的变量一一对应：四个「码」变量走 {@link #codes(String)}，
 * 四个「数值」变量走 {@link #number(String)}；首页里没有的维度一律留 null，
 * 由求值器判成不命中——缺数据不等于满足条件。
 *
 * @param mainDiag          主要诊断编码（ZYZD）
 * @param mainOpers         主要手术操作编码（ZYSS，首页可有多条主手术标记）
 * @param otherDiags        其他诊断编码（QTZD）
 * @param otherOpers        其他手术操作编码（QTSS）
 * @param gender            性别（XB，1-男 2-女）
 * @param age               年龄数字，配合 ageUnit
 * @param ageUnit           年龄单位（1-岁 2-月 3-天）
 * @param admissionWeightG  入院体重（克，XSRTZ；新生儿首页只有出生体重，取同一量纲的出生体重代替）
 */
public record DrgFacts(String mainDiag,
                       List<String> mainOpers,
                       List<String> otherDiags,
                       List<String> otherOpers,
                       Integer gender,
                       Integer age,
                       Integer ageUnit,
                       Integer admissionWeightG) {

    public static final String VAR_MAIN_DIAG = "ZYZD";
    public static final String VAR_OTHER_DIAG = "QTZD";
    public static final String VAR_MAIN_OPER = "ZYSS";
    public static final String VAR_OTHER_OPER = "QTSS";
    public static final String VAR_AGE_YEAR = "NL";
    public static final String VAR_GENDER = "XB";
    public static final String VAR_NEONATAL_DAY = "XSRTL";
    public static final String VAR_ADMISSION_WEIGHT = "XSRTZ";

    public DrgFacts {
        mainDiag = DrgCodes.norm(mainDiag);
        mainOpers = DrgCodes.normList(mainOpers);
        otherDiags = DrgCodes.normList(otherDiags);
        otherOpers = DrgCodes.normList(otherOpers);
    }

    /**
     * 变量取值：码类变量返回编码列表（空列表=该维度无值），数值类变量返回空列表。
     */
    public List<String> codes(String var) {
        return switch (var) {
            case VAR_MAIN_DIAG -> List.of(mainDiag);
            case VAR_OTHER_DIAG -> otherDiags;
            case VAR_MAIN_OPER -> mainOpers;
            case VAR_OTHER_OPER -> otherOpers;
            default -> List.of();
        };
    }

    /**
     * 数值类变量取值；无法从首页换算的（如年龄单位是「月」时的出生日龄）返回 null，判成不命中。
     */
    public Integer number(String var) {
        return switch (var) {
            // 周岁：首页按天/月记年龄的，不满 1 岁记 0 周岁（MDC P 的围手术期规则按 NL=0 判定）
            case VAR_AGE_YEAR -> age == null ? null
                    : (Integer.valueOf(AgeUnitEnum.YEAR.getCode()).equals(ageUnit) ? age : 0);
            case VAR_GENDER -> gender;
            // 出生日龄：只有首页按「天」记年龄时才是日龄，按「月」记的换算不出准确天数
            case VAR_NEONATAL_DAY -> Integer.valueOf(AgeUnitEnum.DAY.getCode()).equals(ageUnit) ? age : null;
            case VAR_ADMISSION_WEIGHT -> admissionWeightG;
            default -> null;
        };
    }
}
