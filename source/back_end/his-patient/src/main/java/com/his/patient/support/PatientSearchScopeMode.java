package com.his.patient.support;

/**
 * 全局患者搜索的**取数模式**，由当前登录角色的岗位性质决定（见 PatientSearchScopeResolver）。
 */
public enum PatientSearchScopeMode {

    /**
     * 门诊岗位：先今日就诊（本人的最前），再全院档案。当前行为保持不变。
     */
    TODAY_FIRST,

    /**
     * 纯档案：不查候诊队列、不参与排序、不回填 todayVisit* 标注。
     *
     * <p>前端因为「只有一组不打标题」自动退化成单列表，不需要按角色各写一套渲染逻辑。
     */
    ARCHIVE_ONLY
}
