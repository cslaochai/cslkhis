package com.his.outpatient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.PageResult;
import com.his.outpatient.dto.NurseScheduleTemplateDTO;
import com.his.outpatient.vo.NurseScheduleTemplateVO;

import java.util.List;

/**
 * 护理排班模板服务：模板/规则的增删改查、统计，以及「套用模板到周」生成真实护士周排班。
 */
public interface NurseScheduleTemplateService {

    /** 首页四张统计卡 */
    NurseScheduleTemplateVO.Stats stats();

    /** 模板分页（delFlag=0） */
    PageResult<NurseScheduleTemplateVO.Template> pageTemplates(NurseScheduleTemplateDTO.PageQuery dto);

    /** 模板详情（含明细行） */
    NurseScheduleTemplateVO.Template detail(Long id);

    /** 新增/修改模板（合一，主表 + 明细同事务替换） */
    void upsert(NurseScheduleTemplateDTO.Upsert dto);

    /** 逻辑删除模板（置 delFlag=1，联动明细） */
    boolean delete(Long id);

    /** 启停模板 */
    boolean updateStatus(Long id, Integer status);

    /** 规则列表（含停用行） */
    List<NurseScheduleTemplateVO.Rule> listRules();

    /** 新增/修改规则 */
    void upsertRule(NurseScheduleTemplateDTO.RuleUpsert dto);

    /** 逻辑删除规则 */
    boolean deleteRule(Long id);

    /** 套用模板到目标周，生成护士周排班（来源=TEMPLATE），并写适用记录 */
    NurseScheduleTemplateVO.Apply applyToWeek(NurseScheduleTemplateDTO.Apply dto);

    /** 适用记录分页（按套用时间倒序） */
    PageResult<NurseScheduleTemplateVO.Apply> pageApplies(NurseScheduleTemplateDTO.ApplyPageQuery dto);
}
