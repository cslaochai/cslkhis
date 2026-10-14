package com.mis.service;

import com.mis.common.base.PageResult;
import com.mis.dto.PersonQueryDTO;
import com.mis.dto.PersonSaveDTO;
import com.mis.entity.JinbInsuredPerson;

public interface InsuredPersonService {

    /** 参保查询（按身份证/医保卡号/姓名） */
    JinbInsuredPerson query(PersonQueryDTO dto);

    /** 管理端分页 */
    PageResult<JinbInsuredPerson> page(String keyword, int pageNum, int pageSize);

    /** 新增/同步参保人（按身份证幂等） */
    JinbInsuredPerson save(PersonSaveDTO dto);
}
