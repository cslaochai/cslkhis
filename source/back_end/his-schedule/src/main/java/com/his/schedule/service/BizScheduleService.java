package com.his.schedule.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.schedule.dto.NurseScheduleDTO;
import com.his.schedule.entity.BizSchedule;
import com.his.schedule.vo.NurseScheduleVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 病区护理排班服务
 */
public interface BizScheduleService extends IService<BizSchedule> {

    /**
     * 病区下拉：只列当前岗位可见科室下的启用病区
     */
    List<NurseScheduleVO.Ward> wardSelectList(String keyword);

    /** 护士下拉：病区所属科室的在册护士（emp_type 5-护士 6-护师） */
    /**
     * 单元候选（sql/209 起含病区 + 门诊科室；只列病区的旧接口保留兼容）
     */
    List<NurseScheduleVO.Ward> unitSelectList(String keyword);

    /**
     * 在册护士候选下拉：取指定排班单元（病区/门诊科室）所属科室的护士与护师，支持按姓名模糊搜。
     * 供排班选人环节按需取数（单元先由单元候选定下来，人和单元是成对查询的）。
     */
    List<NurseScheduleVO.Nurse> nurseSelectList(Integer unitType, Long unitId, String keyword);

    /**
     * 周矩阵：行=在册护士，列=周一至周日，附本周告警与每日在岗人数对照
     */
    NurseScheduleVO.Matrix weekMatrix(NurseScheduleDTO.MatrixQuery query);

    /**
     * 排班台账分页（跨病区回看，数据范围按科室收口）
     */
    PageResult<NurseScheduleVO.Row> listPage(NurseScheduleDTO.PageQuery query);

    /**
     * 点格排班/改格（一人一天一条，撞唯一键即覆盖原走向）。
     *
     * @return 该行 + 本人当周工时 + 相关告警
     */
    NurseScheduleVO.SaveResult upsert(NurseScheduleDTO.CellUpsert dto);

    /**
     * 删除一格（物理删：唯一键不含 del_flag，软删会让「重排同一人同一天」撞键）
     */
    NurseScheduleVO.DeleteResult deleteById(Long id);

    /**
     * 复制上周：只填目标周空缺格，已排的不覆盖
     */
    NurseScheduleVO.CopyResult copyWeek(NurseScheduleDTO.CopyWeek dto);

    /**
     * 批量铺班：共用「状态+班次」× 选中日期 × 多选护士笛卡尔展开，只填空缺格、已排的跳过不覆盖，单事务。
     */
    NurseScheduleVO.BatchResult batchUpsert(NurseScheduleDTO.BatchUpsert dto);

    /**
     * 规则校验（区间；缺省按 weekStart 那一周）
     */
    NurseScheduleVO.CheckResult check(NurseScheduleDTO.CheckQuery query);

    /**
     * 月度工时统计（含未排班的人，空白也看得见）
     */
    NurseScheduleVO.MonthWorkload monthWorkload(NurseScheduleDTO.WorkloadQuery query);

    /**
     * 本病区人力配置标准（含停用行，维护页要能看见为什么不生效）
     */
    List<NurseScheduleVO.Rule> ruleList(Long wardId);

    /**
     * 单元维度的人力配置标准（病区与有护理编制的门诊科室共用，含停用行）。
     * 用于病区护理排班页「人力配置标准」标签页的列表与保存后重拉。
     */
    List<NurseScheduleVO.Rule> ruleList(Integer unitType, Long unitId);

    /**
     * 保存人力配置标准（shiftId=0 为病区级行）
     */
    void ruleUpsert(NurseScheduleDTO.RuleUpsert dto);

    /**
     * 删除标准行（物理删）
     */
    void ruleDeleteById(Long id);

    /**
     * 区间告警明细（周矩阵内嵌与 /check 接口共用同一口径）
     */
    List<NurseScheduleVO.Warning> warningsOf(Long wardId, LocalDate startDate, LocalDate endDate);

    // ===== G-15 发布态流转（提交→审核→发布 / 调班 / 撤回）=====
    /** 提交审核：草稿 → 待审核 */
    void submit(Long id);

    /** 审核发布：待审核 → 已发布（此后才计在岗） */
    void auditPublish(Long id);

    /** 撤回：任意态 → 草稿（不计在岗） */
    void recall(Long id);

    /** 进入调班：已发布/已变更 → 调班中（锁定，改完保存转已变更） */
    void adjustStart(Long id);

    /**
     * 整周提交审核：本周本病区/科室的草稿(0) → 待审核(1)，返回迁移条数。
     * 已发布/已变更/调班中行不动。
     */
    int submitWeek(NurseScheduleDTO.MatrixQuery query);

    /**
     * 整周审核发布：本周本病区/科室的待审核(1) → 已发布(2)，返回迁移条数。
     * 此后这些行计入在岗；草稿/调班中行不动。
     */
    int auditWeek(NurseScheduleDTO.MatrixQuery query);
}
