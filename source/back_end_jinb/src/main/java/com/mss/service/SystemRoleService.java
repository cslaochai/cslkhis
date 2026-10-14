package com.mss.service;

import com.mss.common.base.PageResult;
import com.mss.dto.RoleUpsertDTO;
import com.mss.entity.MssRole;
import com.mss.vo.RoleVO;

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
}
