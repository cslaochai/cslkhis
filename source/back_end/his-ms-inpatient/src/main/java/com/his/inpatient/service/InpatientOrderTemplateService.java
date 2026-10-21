package com.his.inpatient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.inpatient.dto.InpatientOrderTemplatePageQueryDTO;
import com.his.inpatient.dto.InpatientOrderTemplateUpsertDTO;
import com.his.inpatient.vo.InpatientOrderTemplateDetailVO;
import com.his.inpatient.vo.InpatientOrderTemplateListVO;
import com.his.inpatient.vo.InpatientOrderTemplateSelectListVO;

import java.util.List;

/**
 * 住院医嘱模板服务（医生个人模板：开立弹窗「套用模板 / 另存为模板」）。
 */
public interface InpatientOrderTemplateService {

    /**
     * 新增/修改模板，返回模板ID
     */
    Long upsert(InpatientOrderTemplateUpsertDTO dto);

    /**
     * 模板明细（含明细行），供套用与预览
     */
    InpatientOrderTemplateDetailVO getById(Long id);

    /**
     * 当前医生的模板分页（模板管理弹窗）
     */
    IPage<InpatientOrderTemplateListVO> listPage(InpatientOrderTemplatePageQueryDTO query);

    /**
     * 当前医生的模板下拉候选（开立弹窗，按创建时间倒序，有上限）
     */
    List<InpatientOrderTemplateSelectListVO> selectList();

    /**
     * 删除模板（逻辑删主表，明细物理删）
     */
    void deleteById(Long id);
}
