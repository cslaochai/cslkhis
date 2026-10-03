package com.his.miniapp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.miniapp.entity.BizTriageRule;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * 导诊规则 Mapper（本模块自有表）。
 */
@Mapper
public interface BizTriageRuleMapper extends BaseMapper<BizTriageRule> {

    /**
     * 清空全部规则（覆盖式导入用）。
     *
     * <p>必须物理删：{@code uk_symptom_dept(symptom_code,dept_id)} 不含 del_flag，
     * 软删的行仍占着唯一键，先软删再插同一症状+科室必然 Duplicate entry。
     */
    @Delete("DELETE FROM biz_triage_rule")
    int purgeAll();
}
