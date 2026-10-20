package com.his.common.vo;

import com.his.common.util.TextUtil;

/**
 * 科室引用（ID + 名称快照）。
 */
public record DeptRefVO(Long deptId, String deptName) {
    public boolean isEmpty() {
        return deptId == null && !TextUtil.hasText(deptName);
    }
}
