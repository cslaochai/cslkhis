package com.his.emr.service;

import com.his.emr.dto.BizInspectionTemplateUpsertDTO;
import com.his.emr.dto.BizLaboratoryTemplateUpsertDTO;
import com.his.emr.vo.BizInspectionTemplateVO;
import com.his.emr.vo.BizLaboratoryTemplateVO;

import java.util.List;

/**
 * 医生工作站服务接口
 */
public interface InspectionTemplService {
    /**
     * 查询检查申请模板列表（操作人取当前登录用户，未登录返回空列表）
     */
    List<BizInspectionTemplateVO> getInspectionTemplateList();

    /**
     * 新增检查申请模板；取不到当前登录用户时抛业务异常
     */
    boolean addInspectionTemplate(BizInspectionTemplateUpsertDTO upsertDTO);

    /**
     * 删除检查申请模板
     */
    boolean deleteInspectionTemplate(Long id);

    /**
     * 查询检验申请模板列表（操作人取当前登录用户，未登录返回空列表）
     */
    List<BizLaboratoryTemplateVO> getLaboratoryTemplateList();

    /**
     * 新增检验申请模板；取不到当前登录用户时抛业务异常
     */
    boolean addLaboratoryTemplate(BizLaboratoryTemplateUpsertDTO upsertDTO);

    /**
     * 删除检验申请模板
     */
    boolean deleteLaboratoryTemplate(Long id);

}
