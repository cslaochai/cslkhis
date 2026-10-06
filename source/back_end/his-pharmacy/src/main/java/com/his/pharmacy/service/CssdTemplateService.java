package com.his.pharmacy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.pharmacy.dto.CssdDTO;
import com.his.pharmacy.vo.CssdPackTemplateItemSelectListVO;
import com.his.pharmacy.vo.CssdPackTemplateSelectListVO;
import com.his.pharmacy.vo.CssdPackTemplateVO;

import java.util.List;

public interface CssdTemplateService {

    List<CssdPackTemplateSelectListVO> selectList();

    IPage<CssdPackTemplateVO> listPage(CssdDTO.TemplateQueryPage q);

    CssdPackTemplateVO getDetailById(Long templateId);

    List<CssdPackTemplateItemSelectListVO> itemSelectList();

    CssdPackTemplateVO upsert(CssdDTO.TemplateUpsert dto);

    void deleteById(Long templateId);
}
