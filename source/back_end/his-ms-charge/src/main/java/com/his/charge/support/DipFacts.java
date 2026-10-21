package com.his.charge.support;

import java.util.List;

/**
 * DIP 分组事实（一次入组所需的病案维度，编码已归一）。
 *
 * <p>DIP 病种 = 主诊断 + 手术操作组合，辅助目录按 年龄 / 合并症并发症 / 肿瘤分级 调整系数，
 * 所以事实只需这五个维度；性别、体重等 DRG 规则用到的维度 DIP 不需要，不取。
 *
 * @param mainDiag  主要诊断编码（首页主诊断，整码归一）
 * @param mainOpers 主要手术操作编码（首页可标多条，核心病种按目录行逐个整码匹配）
 * @param otherDiags 其他诊断编码（辅助目录的合并症/肿瘤条件按此判定）
 * @param age       年龄数字，配合 ageUnit
 * @param ageUnit   年龄单位（1-岁 2-月 3-天；不满 1 岁按 0 周岁参与年龄型辅助目录）
 */
public record DipFacts(String mainDiag,
                       List<String> mainOpers,
                       List<String> otherDiags,
                       Integer age,
                       Integer ageUnit) {

    public DipFacts {
        mainDiag = DrgCodes.norm(mainDiag);
        mainOpers = DrgCodes.normList(mainOpers);
        otherDiags = DrgCodes.normList(otherDiags);
    }

    /**
     * 周岁：首页按天/月记年龄的，不满 1 岁记 0 周岁（与 DRG 规则里的 NL 口径一致）。
     */
    public Integer ageYears() {
        if (age == null) {
            return null;
        }
        return Integer.valueOf(1).equals(ageUnit) ? age : 0;
    }
}
