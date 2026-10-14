package com.mis.service;

import com.mis.common.base.PageResult;
import com.mis.dto.RoleMenuSaveDTO;
import com.mis.dto.RoleUpsertDTO;
import com.mis.entity.MssRole;
import com.mis.vo.RoleVO;

import java.util.List;

/**
 * 角色管理
 */
public interface SystemRoleService {

    PageResult<MssRole> page(String keyword, long pageNum, long pageSize);

    List<MssRole> selectList();

    RoleVO getDetailById(Long id);

    MssRole upsert(RoleUpsertDTO dto);

    void deleteById(Long id);

    /** 角色已授权的菜单 ID（HIS：getMenuIds，字符串列表供树回显） */
    List<String> getMenuIds(Long roleId);

    /** 角色菜单授权整单替换（HIS：saveRoleMenu） */
    void saveRoleMenu(RoleMenuSaveDTO dto);
}
