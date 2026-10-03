package com.his.patient.service;

import com.his.common.base.PageResult;
import com.his.patient.dto.NurseScheduleDTO;
import com.his.patient.vo.NurseScheduleVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 病区护理排班服务（sql/166）。
 *
 * <p>三件事：周矩阵读写、人力标准（规则）维护、规则校验。
 * 校验一律<b>告警不拦截</b> —— 急诊抽调、临时加床都会造成合理缺口，
 * 拦住保存只会逼人把班表排成「好看但没有货」的样子。
 */
public interface NurseScheduleService {

    /** 病区下拉：只列当前岗位可见科室下的启用病区 */
    List<NurseScheduleVO.Ward> wardSelectList(String keyword);

    /** 护士下拉：病区所属科室的在册护士（emp_type 5-护士 6-护师） */
    /** 单元候选（sql/209 起含病区 + 门诊科室；只列病区的旧接口保留兼容） */
    List<NurseScheduleVO.Ward> unitSelectList(String keyword);

    List<NurseScheduleVO.Nurse> nurseSelectList(Integer unitType, Long unitId, String keyword);

    /** 周矩阵：行=在册护士，列=周一至周日，附本周告警与每日在岗人数对照 */
    NurseScheduleVO.Matrix weekMatrix(NurseScheduleDTO.MatrixQuery query);

    /** 排班台账分页（跨病区回看，数据范围按科室收口） */
    PageResult<NurseScheduleVO.Row> listPage(NurseScheduleDTO.QueryPage query);

    /**
     * 点格排班/改格（一人一天一条，撞唯一键即覆盖原走向）。
     *
     * @return 该行 + 本人当周工时 + 相关告警
     */
    NurseScheduleVO.SaveResult upsert(NurseScheduleDTO.CellUpsert dto);

    /** 删除一格（物理删：唯一键不含 del_flag，软删会让「重排同一人同一天」撞键） */
    NurseScheduleVO.DeleteResult deleteById(Long id);

    /** 复制上周：只填目标周空缺格，已排的不覆盖 */
    NurseScheduleVO.CopyResult copyWeek(NurseScheduleDTO.CopyWeek dto);

    /** 规则校验（区间；缺省按 weekStart 那一周） */
    NurseScheduleVO.CheckResult check(NurseScheduleDTO.CheckQuery query);

    /** 月度工时统计（含未排班的人，空白也看得见） */
    NurseScheduleVO.MonthWorkload monthWorkload(NurseScheduleDTO.WorkloadQuery query);

    /** 本病区人力配置标准（含停用行，维护页要能看见为什么不生效） */
    List<NurseScheduleVO.Rule> ruleList(Long wardId);

    List<NurseScheduleVO.Rule> ruleList(Integer unitType, Long unitId);

    /** 保存人力配置标准（shiftId=0 为病区级行） */
    void ruleUpsert(NurseScheduleDTO.RuleUpsert dto);

    /** 删除标准行（物理删） */
    void ruleDeleteById(Long id);

    /** 区间告警明细（周矩阵内嵌与 /check 接口共用同一口径） */
    List<NurseScheduleVO.Warning> warningsOf(Long wardId, LocalDate startDate, LocalDate endDate);
}
