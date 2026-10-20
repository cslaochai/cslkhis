package com.his.common.enums;

/**
 * 门诊诊查费职级加收档位。
 *
 * <p><b>政策依据</b>：湘医保发〔2025〕58号《关于规范整合综合诊查类医疗服务价格项目的通知》
 * （长沙市医保局同文，2026-01-01 执行）附件1「门诊诊查费(普通门诊)」把原来的挂号费、
 * 门诊诊查费、检查费、治疗费合并为一项，并按医师职级设四档加收价。
 *
 * <table>
 *   <caption>四档价格</caption>
 *   <tr><th>档位</th><th>国家编码</th><th>加收额</th><th>全额</th></tr>
 *   <tr><td>普通</td><td>011102020010000</td><td>—</td><td>9</td></tr>
 *   <tr><td>副主任医师</td><td>011102020010001</td><td>13</td><td>22</td></tr>
 *   <tr><td>主任医师</td><td>011102020010002</td><td>22</td><td>31</td></tr>
 *   <tr><td>知名专家</td><td>011102020010003</td><td>61</td><td>70</td></tr>
 * </table>
 *
 * <p><b>为什么是四档而不是一个专家号开关</b>：改版前号源只有 {@code isExpert} 二值，
 * 前端把「副主任」和「主任」都归成专家号，两个职级被压成同一档价格（历史数据里全是 70 元）。
 * 政策层面这两个职级是<b>不同</b>的加收价，必须分开计价。
 *
 * <p>本枚举只表达「这个号源适用哪一档加收」。实际金额仍落在号源表的 {@code diagnosisFee} 上——
 * 价格是<b>号源快照</b>而不是运行时推导：医生升职称后，已排出去的号源价格不能跟着变。
 */
public enum ClinicDiagnosisTierEnum {

    /** 主治医师及以下，不加收，9 元 */
    GENERAL(1, "普通", "011102020010000", null, "9.00"),

    /** 副主任医师，加收 13 元，全额 22 元 */
    DEPUTY_CHIEF(2, "副主任医师", "011102020010001", "13.00", "22.00"),

    /** 主任医师，加收 22 元，全额 31 元 */
    CHIEF(3, "主任医师", "011102020010002", "22.00", "31.00"),

    /** 知名专家，加收 61 元，全额 70 元 */
    EXPERT(4, "知名专家", "011102020010003", "61.00", "70.00");

    private final Integer code;
    private final String label;
    private final String ybCode;
    private final String surcharge;
    private final String amount;

    ClinicDiagnosisTierEnum(Integer code, String label, String ybCode, String surcharge, String amount) {
        this.code = code;
        this.label = label;
        this.ybCode = ybCode;
        this.surcharge = surcharge;
        this.amount = amount;
    }

    public Integer getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    /** 该档加收项的国家医保编码（普通档即主项编码） */
    public String getYbCode() {
        return ybCode;
    }

    /** 该档加收额（元），普通档为 null 表示无加收 */
    public String getSurcharge() {
        return surcharge;
    }

    /** 该档全额（元） */
    public String getAmount() {
        return amount;
    }

    public static ClinicDiagnosisTierEnum ofCode(Integer code) {
        if (code != null) {
            for (ClinicDiagnosisTierEnum e : values()) {
                if (e.code.equals(code)) {
                    return e;
                }
            }
        }
        return null;
    }
}
