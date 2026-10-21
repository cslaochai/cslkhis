package com.his.clinical.service;

import com.his.clinical.dto.BizRxTemplateUpsertDTO;
import com.his.clinical.dto.DiagTemplateUpsertDTO;
import com.his.clinical.vo.BizDiagTemplateVO;
import com.his.clinical.vo.BizRxTemplateVO;

import java.util.List;

/**
 * 医生个人模板服务：常用诊断、处方模板两类医生私有数据的业务收口，归属一律取当前登录医生。
 * 药品/耗材套餐归属药房域，不在本接口。
 */
public interface OtherTemplateService {

    // 常用诊断
    List<BizDiagTemplateVO> listDiagTemplates();

    /**
     * 整表覆盖当前医生的常用诊断清单：先清空名下所有条目再按入参顺序重排序号，入参缺省视为清空。
     * 用于「常用诊断维护」弹框增删后的整体保存，以及病历诊断区「加入常用诊断」。
     */
    boolean saveDiagTemplates(DiagTemplateUpsertDTO saveDTO);

    /**
     * 摘掉一条常用诊断：按主键删单个条目，不动其余清单。
     * 用于「常用诊断维护」弹框列表行的删除。
     */
    boolean deleteDiagTemplate(Long id);

    // 处方模板
    List<BizRxTemplateVO> listRxTemplates();

    /**
     * 按模板主键取处方模板的药品明细行。
     * 用于医生工作站「处方模板管理」弹框点「套用」时回填当前处方。
     */
    BizRxTemplateVO getRxTemplateDetail(Long templateId);

    /**
     * 新建一张处方模板：主表与药品明细一并落库，药品数与合计金额由服务端按明细汇总。
     * 用于医生工作站「处方模板管理」弹框的「保存当前处方为模板」。
     */
    boolean saveRxTemplate(BizRxTemplateUpsertDTO upsertDTO);

    /**
     * 删除一张处方模板：连名下药品明细一起清掉。
     * 用于「处方模板管理」弹框列表行的删除。
     */
    boolean deleteRxTemplate(Long id);
}
