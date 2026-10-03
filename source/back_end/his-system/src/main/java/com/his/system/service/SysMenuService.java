package com.his.system.service;

import com.his.system.dto.MenuUpsertDTO;
import com.his.system.vo.MenuVO;

import java.util.List;

public interface SysMenuService {

    /**
     * 全量菜单树。
     *
     * <p>顺序必须确定：显式按排序字段升序，且排序字段允许并列，必须再补主键作兜底键，
     * 否则并列行的先后取决于执行计划 → 同角色两次登录侧栏可能不同。
     */
    List<MenuVO> tree();

    MenuVO getInfo(Long menuId);

    /**
     * 当前登录人**当前角色**的菜单树（token 里的 currentRole，不是员工全部角色的并集）。
     *
     * <p>员工没挂角色（或 token 未带角色）时退回按员工取全部角色菜单，避免直接登录变成空菜单。
     */
    List<MenuVO> userMenus();

    /**
     * @return 面向用户的操作结果文案（新增/修改两条路文案不同）
     */
    String upsert(MenuUpsertDTO upsertDTO);

    void delete(Long menuId);
}
