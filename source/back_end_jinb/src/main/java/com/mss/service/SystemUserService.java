package com.mss.service;

import com.mss.common.base.PageResult;
import com.mss.dto.ResetPasswordDTO;
import com.mss.dto.UserQueryPageDTO;
import com.mss.dto.UserStatusDTO;
import com.mss.dto.UserUpsertDTO;
import com.mss.entity.MssUser;
import com.mss.vo.UserVO;

/**
 * 用户管理
 */
public interface SystemUserService {

    PageResult<UserVO> page(UserQueryPageDTO dto);

    MssUser upsert(UserUpsertDTO dto);

    void resetPassword(ResetPasswordDTO dto);

    void changeStatus(UserStatusDTO dto);

    void deleteById(Long id);
}
