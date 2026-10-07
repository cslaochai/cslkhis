package com.his.appoint.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.appoint.dto.ScheduleTemplateQueryPageDTO;
import com.his.appoint.dto.ScheduleTemplateUpsertDTO;
import com.his.appoint.entity.BizScheduleTemplate;
import com.his.appoint.vo.ScheduleTemplatePreviewVO;
import com.his.appoint.vo.ScheduleTemplateVO;
import com.his.common.base.PageResult;

import java.util.List;

/**
 * 排班模板服务
 */
public interface BizScheduleTemplateService extends IService<BizScheduleTemplate> {

    /**
     * 给模板 VO 批量补「班次名 + 班别」：这两项不落模板表，一律按 shift_id 从班次字典带出
     */
    void fillShiftDisplay(List<ScheduleTemplateVO> voList);

    /**
     * 模板列表（可按科室/岗位类别/星期几/状态过滤）
     *
     * @param staffType 岗位类别（sql/195，空=全部岗位）
     */
    List<BizScheduleTemplate> listTemplates(Long deptId, Integer staffType, Integer weekDay, Integer status);

    /**
     * 模板列表出参：口径同 {@link #listTemplates}，并按班次字典补齐班别/班次名。
     */
    List<ScheduleTemplateVO> listVO(Long deptId, Integer staffType, Integer weekDay, Integer status);

    /**
     * 模板分页出参：关键词对科室/医生/诊室/备注模糊，排序按 星期几+开始时间+id 二级键。
     */
    PageResult<ScheduleTemplateVO> pageVO(ScheduleTemplateQueryPageDTO dto);

    /**
     * 新增/修改模板（入参即前端表单）：落库失败按新增/修改分别报错。
     * 段级号源配置随单提交：null=不动 / []=清空（回退半小时均分）/ 非空=整批替换，
     * 与主表同事务生效。
     */
    void upsertTemplate(ScheduleTemplateUpsertDTO dto);

    /**
     * 新增/修改模板（同医生同星期几同班次拒重；划池数不得超过总号源）
     */
    boolean saveTemplate(BizScheduleTemplate template);

    /**
     * 删除模板（逻辑删）
     */
    boolean deleteTemplate(Long id);

    /**
     * 模板启停（只更新状态列）
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 按模板生成目标周的排班。weekOffset：0=本周 1=下周（默认）-1=上周。
     * 返回结论文案（新增/跳过明细）。
     *
     * @param staffType 岗位类别（sql/195，空=全部岗位）：排班员通常按岗位分批生成，
     *                  医生出诊模板与出勤岗模板的规则不同（号源/诊室只对医生有意义）
     */
    String generateForWeek(Integer weekOffset, Long deptId, Integer staffType);

    /**
     * 生成预览（dryRun 不落库）：将新增/各类跳过明细 + 涉及人员名单（人工核对出诊人）
     *
     * @param staffType 岗位类别（sql/195，空=全部岗位）
     */
    ScheduleTemplatePreviewVO previewForWeek(Integer weekOffset, Long deptId, Integer staffType);
}
