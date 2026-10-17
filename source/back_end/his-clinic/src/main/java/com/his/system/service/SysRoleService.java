package com.his.system.service;

import com.his.common.base.PageResult;
import com.his.system.dto.RoleMenuUpsertDTO;
import com.his.system.dto.SysRoleQueryDTO;
import com.his.system.dto.SysRoleQueryPageDTO;
import com.his.system.dto.SysRoleUpsertDTO;
import com.his.system.vo.RoleSelectListVO;
import com.his.system.vo.RoleVO;

import java.util.List;

public interface SysRoleService {

    PageResult<RoleVO> listPage(SysRoleQueryPageDTO queryDTO);

    List<RoleSelectListVO> selectList(SysRoleQueryDTO queryDTO);

    RoleVO getInfo(Long roleId);

    /**
     * @return 面向用户的操作结果文案（新增/修改两条路文案不同）
     */
    String upsert(SysRoleUpsertDTO upsertDTO);

    void delete(Long roleId);

    List<Long> getMenuIds(Long roleId);

    void saveRoleMenu(RoleMenuUpsertDTO upsertDTO);
}
