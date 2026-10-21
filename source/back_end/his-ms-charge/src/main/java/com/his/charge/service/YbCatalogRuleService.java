package com.his.charge.service;

import com.his.charge.dto.YbCatalogRuleImportDTO;
import com.his.charge.vo.YbImportResultVO;

import java.util.List;

/**
 * 医保目录报销规则服务。
 *
 * <p>规则表是「项目 × 费别 × 就诊类型 × 险种」四维配置，直接决定统筹金额。
 * 目录表能批量导、规则表以前只能靠 SQL 塞，两者不对称 —— 补上这个入口。
 */
public interface YbCatalogRuleService {

    /**
     * 规则批量导入（按 rule_no 幂等，已存在即更新）。
     *
     * @param items 规则明细行
     * @return 新增/更新/跳过条数
     */
    YbImportResultVO importBatch(List<YbCatalogRuleImportDTO> items);
}