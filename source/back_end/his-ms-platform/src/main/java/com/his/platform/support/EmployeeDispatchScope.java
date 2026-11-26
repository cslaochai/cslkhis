package com.his.platform.support;

import java.util.Map;
import java.util.Set;

/**
 * 人事调配员的可见人员类别收口：调度角色只看自己那条线的在岗人员，名册里不该冒出别线的人。
 *
 * <p>映射口径（按当前生效角色 {@code currentRole} 取）：
 * <ul>
 *   <li>护理调配员（10044）→ 只看护理岗（staff_type=2）；</li>
 *   <li>医务调配员（10045）→ 看医生（1）+ 医技（3）+ 药学（4）。</li>
 * </ul>
 * 调度角色自身在 {@code sys_role.staff_type} 上挂的是行政其他（6），不能用角色自身类别反推主管类别，
 * 所以在这里显式登记，角色编码是稳定标识（id 随库重建会变）。
 */
public final class EmployeeDispatchScope {

    /** 护理调配员角色编码 */
    public static final String ROLE_NURSE_DISPATCH = "10044";
    /** 医务调配员角色编码 */
    public static final String ROLE_MEDICAL_DISPATCH = "10045";

    private static final Map<String, Set<Integer>> MANAGED_STAFF_TYPES = Map.of(
            ROLE_NURSE_DISPATCH, Set.of(2),
            ROLE_MEDICAL_DISPATCH, Set.of(1, 3, 4)
    );

    private EmployeeDispatchScope() {
    }

    /**
     * @param roleCode 当前生效角色编码
     * @return 该调度角色主管的岗位类别集合；不是调配员角色时返回 {@code null}（不做类别收口）
     */
    public static Set<Integer> managedStaffTypes(String roleCode) {
        if (roleCode == null) {
            return null;
        }
        return MANAGED_STAFF_TYPES.get(roleCode);
    }
}
