package com.his.patient.support;

/**
 * 性别文案（P5.1 定口径，P5.6 追加「未知」，2026-09-23 并入统一口径性别字典）
 *
 * <p><b>口径冲突的判定依据（实测 2026-09-19）</b>：
 * 患者基本信息的表注释写的是"0-女 1-男 2-未知"，但库里实际是
 * 刘亦菲/王妮娅 = 2、老王/王托尼/老刘 = 1，且 {@code PatientVO} 与前端
 * genderMap = {1:'男', 2:'女'} 都按 1-男 2-女走 ——
 * <b>以数据与实际代码为准，表注释是错的</b>（已由 sql/44 纠正）。
 *
 * <p>全库的患者性别只有患者基本信息一个源头：挂号、门诊病历、检查 / 检验记录、
 * 住院证、住院文书、医保结算清单上的 gender 都是 {@code setGender(patient.getGender())}
 * 复制过去的快照，所以这里定口径等于给全链路定口径。
 *
 * <p>2026-09-23（sql/75）起与员工性别统一为同一张字典性别字典：
 * <b>1-男 2-女 9-未知</b>（9 取 GB/T 2261.1「未说明」档位；原员工 0-女 1-男已迁移，
 * 原患者未知 3 已迁移为 9）。员工.gender 不再是另一套口径，
 * 渲染员工性别同样可以直接用本类。
 *
 * <p>{@code gender = 0} 的少量历史行属于脏数据：按老表注释会被渲染成"女"，
 * 那是**编造一个性别**。这里一律报未知(0)，让问题暴露出来。
 */
public final class PatientGenderText {

    private PatientGenderText() {
    }

    public static String of(Integer gender) {
        if (gender == null) {
            return "—";
        }
        return switch (gender) {
            case 1 -> "男";
            case 2 -> "女";
            case 9 -> "未知";
            default -> "未知(" + gender + ")";
        };
    }
}
