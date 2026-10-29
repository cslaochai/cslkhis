package com.his.platform.service;

import com.his.common.base.PageResult;
import com.his.platform.dto.RoleMenuUpsertDTO;
import com.his.platform.dto.SysRoleQueryDTO;
import com.his.platform.dto.SysRolePageQueryDTO;
import com.his.platform.dto.SysRoleUpsertDTO;
import com.his.platform.vo.RoleSelectListVO;
import com.his.platform.vo.RoleVO;

import java.util.List;

/** 角色服务：角色档案维护与角色-菜单授权的业务收口，授权变更即时作用于登录态权限。 */
public interface SysRoleService {

    /**
     * 角色分页：按角色名称模糊、状态筛选，结果按排序号升序。
     * 用于系统管理「角色管理」页的查询卡与角色表格。
     */
    PageResult<RoleVO> listPage(SysRolePageQueryDTO queryDTO);

    /**
     * 角色下拉候选：一次取全量不分页，不受当前登录岗位的角色范围限制。
     * 用于给员工或账号分配角色，以及任何「选一个角色」的地方（如工作台角色卡片配置）。
     */
    List<RoleSelectListVO> selectList(SysRoleQueryDTO queryDTO);

    /**
     * 单个角色的完整字段（名称、编码、类型、状态、排序号等）。
     * 用于「角色管理」页「编辑角色」弹框打开时回显表单，新增场景不取数。
     */
    RoleVO getInfo(Long roleId);

    /**
     * @return 面向用户的操作结果文案（新增/修改两条路文案不同）
     */
    String upsert(SysRoleUpsertDTO upsertDTO);

    /**
     * 删除角色：系统内置角色拒绝删除，删除同时清掉该角色的菜单授权并失效权限与账号身份缓存。
     * 用于「角色管理」列表行的删除按钮，前端确认后调用。
     */
    void delete(Long roleId);

    /**
     * 角色已授权的菜单标识清单：只回该角色当前勾选的节点，不含树结构与未授权节点。
     * 用于「角色管理」页「配置菜单权限」弹框打开时初始化权限树勾选。
     */
    List<Long> getMenuIds(Long roleId);

    /**
     * 保存角色的菜单授权：以传入集合为该角色最终权限（整表替换，缺失即取消），空集合表示清空。
     * 用于「角色管理」页「配置菜单权限」弹框保存；保存后权限缓存即时失效，下一个请求按新授权收口。
     *
     * @param upsertDTO 子孙节点会自动补齐所属父目录——只勾二级菜单而漏掉父目录时侧边栏不显示
     */
    void saveRoleMenu(RoleMenuUpsertDTO upsertDTO);

    /**
     * 手动刷新角色权限缓存：失效该角色的权限缓存（his:perm:role:{roleCode}）
     * 以及持有该角色的所有账号身份缓存（his:user:identity:{username}）。
     * 用于「角色管理」页「刷新缓存」按钮，覆盖绕过服务层直接改库 / TTL 未到期时的强制刷新。
     */
    void refreshPermissionCache(Long roleId);
}
