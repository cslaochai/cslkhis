package com.mss.service;

import com.mss.common.base.PageResult;
import com.mss.dto.PostReplaceDTO;
import com.mss.entity.MssEmployee;
import com.mss.vo.PostVO;

import java.util.List;

/**
 * 员工岗位管理（岗位 = 角色 × 科室）
 */
public interface SystemPostService {

    PageResult<PostVO> page(String keyword, long pageNum, long pageSize);

    List<PostVO> postsOfEmployee(Long employeeId);

    void replacePosts(PostReplaceDTO dto);

    List<MssEmployee> employeeSelect(String keyword);
}
