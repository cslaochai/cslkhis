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
 *
 * <p><b>模板不落库到医嘱表</b>：套用只是把明细回填到开立表单，医生改完仍走
 * {@code /patient/inpatient/order/save}。这样医嘱的双签、组套同起同停、欠费管控、
 * 执行计划与计费快照全部零改动 —— 模板不另开一条写医嘱的路。
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
