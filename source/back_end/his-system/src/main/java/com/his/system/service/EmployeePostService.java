package com.his.system.service;

import com.his.security.entity.CurrentUser;
import com.his.system.dto.EmployeePostDTO;
import com.his.system.vo.EmployeePostVO;
import com.his.system.vo.SwitchPostVO;

import java.util.List;

/**
 * 员工岗位（角色 × 科室）服务。
 */
public interface EmployeePostService {

    /**
     * 该员工被分配的岗位列表（含已失效的，配置页回显用）。
     */
    List<EmployeePostVO> listPosts(Long employeeId);

    /**
     * 该员工**当前生效**的岗位列表（sql/113：切换岗位入口只列这些，切不过去的不该出现在下拉里）。
     */
    List<EmployeePostVO> listActivePosts(Long employeeId);

    /**
     * 登录时该角色的默认落点岗位：该角色下「与本人主岗位同科室」的那条优先，
     * 该角色在这个科室没有岗位时退到它的第一条（主岗位一人只有一条，见 {@link #replacePosts}）。
     *
     * @return null = 该角色一个岗位都没有（既没科室也没法收口），调用方应拒绝登录
     */
    EmployeePostVO resolvePrimaryPost(Long employeeId, String roleCode);

    /**
     * 切换到指定岗位：校验通过才重签 token，并把切换动作写进审计日志。
     *
     * @param user     当前登录用户（方法内会同步更新其 currentRole / deptId / deptName）
     * @param roleCode 目标角色编码
     * @param deptId   目标科室ID
     * @throws com.his.common.exception.BusinessException 该 (角色, 科室) 不是本人的岗位
     */
    SwitchPostVO switchPost(CurrentUser user, String roleCode, Long deptId);

    /**
     * 整体替换某员工的岗位（管理端保存员工/用户时调用）。
     *
     * <p>鉴权（{@code /auth/info} 角色集合、菜单/按钮权限 join）直读员工岗位；
     * 原镜像表旧员工角色表已删除（sql/118）。
     * 主岗位回填到员工的科室ID / dept_name（号源、名册等仍读它）。
     *
     * <p><b>主岗位归一化成「一人一条」</b>：入参勾了多条只认第一条，一条都没勾则把第一条提上去 ——
     * 按「每个角色一条」存过，多角色员工的配置表会排出一排「主岗位」，那不是人事概念。
     *
     * @param posts null = 本次请求不带岗位字段（如医生名册只切换启用状态），**原有岗位一律不动**；
     *              空集合 = 显式清空
     * @return 该员工的主岗位；没有任何岗位时返回 null
     */
    EmployeePostVO replacePosts(Long employeeId, List<EmployeePostDTO> posts);
}
