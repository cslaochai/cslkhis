package com.his.patient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.dto.InpatientOrderTemplateQueryPageDTO;
import com.his.patient.dto.InpatientOrderTemplateUpsertDTO;
import com.his.patient.vo.InpatientOrderTemplateDetailVO;
import com.his.patient.vo.InpatientOrderTemplateListVO;
import com.his.patient.vo.InpatientOrderTemplateSelectListVO;

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
    IPage<InpatientOrderTemplateListVO> listPage(InpatientOrderTemplateQueryPageDTO query);

    /**
     * 当前医生的模板下拉候选（开立弹窗，按创建时间倒序，有上限）
     */
    List<InpatientOrderTemplateSelectListVO> selectList();

    /**
     * 删除模板（逻辑删主表，明细物理删）
     */
    void deleteById(Long id);
}
