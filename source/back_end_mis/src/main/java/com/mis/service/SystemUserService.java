package com.mis.service;

import com.mis.common.base.PageResult;
import com.mis.dto.ResetPasswordDTO;
import com.mis.dto.UserQueryPageDTO;
import com.mis.dto.UserStatusDTO;
import com.mis.dto.UserUpsertDTO;
import com.mis.entity.MssUser;
import com.mis.vo.UserVO;

/**
 * 用户管理
 */
public interface SystemUserService {

    PageResult<UserVO> page(UserQueryPageDTO dto);

    /** 详情（含员工字段与岗位行，HIS：getById） */
    UserVO detail(Long id);

    MssUser upsert(UserUpsertDTO dto);

    void resetPassword(ResetPasswordDTO dto);

    void changeStatus(UserStatusDTO dto);

    void deleteById(Long id);
}
