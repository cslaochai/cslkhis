package com.his.supplies.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.supplies.dto.CssdDTO;
import com.his.supplies.vo.CssdPackTemplateItemSelectListVO;
import com.his.supplies.vo.CssdPackTemplateSelectListVO;
import com.his.supplies.vo.CssdPackTemplateVO;
import java.util.List;

public interface CssdTemplateService {

    List<CssdPackTemplateSelectListVO> selectList();

    IPage<CssdPackTemplateVO> listPage(CssdDTO.TemplateQueryPage q);

    CssdPackTemplateVO getDetailById(Long templateId);

    List<CssdPackTemplateItemSelectListVO> itemSelectList();

    CssdPackTemplateVO upsert(CssdDTO.TemplateUpsert dto);

    void deleteById(Long templateId);
}
