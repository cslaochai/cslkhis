package com.mis.service;

import com.mis.common.base.PageResult;
import com.mis.dto.PostItemDTO;
import com.mis.dto.PostReplaceDTO;
import com.mis.entity.MssEmployee;
import com.mis.vo.PostVO;

import java.util.List;

/**
 * 员工岗位管理（岗位 = 角色 × 科室）
 */
public interface SystemPostService {

    PageResult<PostVO> page(String keyword, long pageNum, long pageSize);

    List<PostVO> postsOfEmployee(Long employeeId);

    void replacePosts(PostReplaceDTO dto);

    /** 供用户管理复用：以指定操作人整单替换某员工岗位（支持 roleCode 入参） */
    void replacePostsOfEmployee(Long employeeId, List<PostItemDTO> posts, String operator);

    List<MssEmployee> employeeSelect(String keyword);
}
