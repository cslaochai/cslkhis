package com.his.emr.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.emr.entity.BizClinicalRuleCheck;
import com.his.emr.vo.BizClinicalRuleCheckVO;

/**
 * 临床规则校验服务接口
 */
public interface ClinicalRuleCheckService extends IService<BizClinicalRuleCheck> {

    /**
     * 查询校验记录列表
     */
    PageResult<BizClinicalRuleCheckVO> selectCheckPage(Long patientId, Integer ruleType,
                                                       Integer checkStatus, int pageNum, int pageSize);

    /**
     * 获取校验详情
     */
    BizClinicalRuleCheckVO getCheckDetail(Long checkId);

    /**
     * 执行临床规则校验
     */
    BizClinicalRuleCheckVO executeCheck(Long recordId, Integer ruleType, String checkBy);

    /**
     * 处理校验问题
     */
    boolean handleCheck(Long checkId, boolean ignore, String remark);
}
