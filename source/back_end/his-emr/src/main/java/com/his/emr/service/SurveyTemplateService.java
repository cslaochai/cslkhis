package com.his.emr.service;

import com.his.common.base.PageResult;
import com.his.emr.dto.SurveyTemplateQueryPageDTO;
import com.his.emr.dto.SurveyTemplateUpsertDTO;
import com.his.emr.vo.SurveyTemplateSelectListVO;
import com.his.emr.vo.SurveyTemplateVO;

import java.util.List;

/**
 * 满意度问卷模板服务（定义「问什么」）。
 */
public interface SurveyTemplateService {

    /**
     * 分页（keyword/scene/status）
     */
    PageResult<SurveyTemplateVO> listPage(SurveyTemplateQueryPageDTO dto);

    /**
     * 下拉：启用中的卷（scene 可空=全部）
     */
    List<SurveyTemplateSelectListVO> selectEnabled(Integer scene);

    /**
     * 详情（含题目清单）
     */
    SurveyTemplateVO getDetailById(Long id);

    /**
     * 新增 / 修改（整卷覆盖题目：物理删旧题再插）
     */
    SurveyTemplateVO upsert(SurveyTemplateUpsertDTO dto);

    /**
     * 删除（软删；已被发放引用时拒绝，让改停用）
     */
    boolean deleteById(Long id);

    /**
     * 取某场景下用于自动发放的卷（启用中、按 id 最新）。
     *
     * @return null = 该场景还没配卷，调用方降级为「不发评价」，绝不建空卷
     */
    SurveyTemplateVO findEnabledForScene(Integer scene);
}
