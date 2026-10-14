package com.mss.service;

import com.mss.common.base.PageResult;
import com.mss.dto.PersonQueryDTO;
import com.mss.dto.PersonSaveDTO;
import com.mss.entity.JinbInsuredPerson;

public interface InsuredPersonService {

    /** 参保查询（按身份证/医保卡号/姓名） */
    JinbInsuredPerson query(PersonQueryDTO dto);

    /** 管理端分页 */
    PageResult<JinbInsuredPerson> page(String keyword, int pageNum, int pageSize);

    /** 新增/同步参保人（按身份证幂等） */
    JinbInsuredPerson save(PersonSaveDTO dto);
}
