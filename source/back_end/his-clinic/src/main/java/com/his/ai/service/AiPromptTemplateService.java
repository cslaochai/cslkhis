package com.his.ai.service;

import com.his.ai.dto.AiPromptTemplatePageQueryDTO;
import com.his.ai.dto.AiPromptTemplateUpsertDTO;
import com.his.ai.vo.AiPromptTemplateVO;
import com.his.common.base.PageResult;

import java.util.List;

/**
 * 提示词模板管理：分页 / 历史版本 / 新增版本 / 版本回退。
 */
public interface AiPromptTemplateService {

    /**
     * 生效版本分页（每模板一行）
     */
    PageResult<AiPromptTemplateVO> listPage(AiPromptTemplatePageQueryDTO query);

    /**
     * 指定编码的历史版本列表（生效版在前，版本新在前）
     */
    List<AiPromptTemplateVO> history(String code);

    /**
     * 新增版本：旧生效版置停用，插入新行，返回新版本号（如 v2.1）
     */
    String upsert(AiPromptTemplateUpsertDTO dto);

    /**
     * 版本回退：把指定历史版本切为生效版，返回确认文案
     */
    String activate(Long id);
}
