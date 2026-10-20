package com.his.schedule.service;

import com.his.common.base.PageResult;
import com.his.schedule.dto.DutyCheckDTO;
import com.his.schedule.dto.DutyRosterPageQueryDTO;
import com.his.schedule.dto.DutyRosterUpsertDTO;
import com.his.schedule.dto.DutySubstituteDTO;
import com.his.schedule.dto.DutyWorkloadDTO;
import com.his.schedule.vo.DutyCheckVO;
import com.his.schedule.vo.DutyOfficerVO;
import com.his.schedule.vo.DutyRosterVO;
import com.his.schedule.vo.DutyWorkloadVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 全院总值班排班服务：排班与临时换班的登记收口，以及「某天某班次谁负责」的解析出口。
 */
public interface DutyRosterService {

    /**
     * 此刻全院谁在值班（凌晨属昨天夜班，按跨自然日的班次归属推断）。
     * 顶栏与急诊、床位中心、转诊页的「今日总值班」条读它；应急升级、跨科调配、转诊协调要找的兜底收口人也是这个口径。
     */
    DutyOfficerVO current();

    /**
     * 指定时刻的总值班，班次归属推断与「此刻」完全同一口径。
     * 供按点验算与回溯（补跑任务、验证脚本要能指定时间点复核当天到底谁在班）。
     */
    DutyOfficerVO currentAt(LocalDateTime at);

    /**
     * 指定「日期 + 班次」的总值班：主班优先，主班漏排则副班顶上，都没有返回未找到并附原因。
     * 排班总览页的「本周每日总值班」栏按天逐班次取数用它，漏排如实显示成没人值班，不替业务兜底。
     *
     * @param shift 班次码值（白班/夜班）
     */
    DutyOfficerVO officerOf(LocalDate date, int shift);

    /**
     * 下一班总值班：白班交同日夜班，夜班交次日白班。
     * 值班日志交班时用它算接班人的默认值——不预设默认人的话，交班人通常会随手挑一个不相干的人。
     */
    DutyOfficerVO nextOfficer(LocalDate date, int shift);

    /**
     * 今天（自然日）的总值班排班：白班/夜班 × 主班/副班，只列行政口径那几条。
     * 总值班排班页的「今日排班」表用它；别和「此刻谁负责」混用——凌晨看这张表是白天的人，在班的却是昨天夜班的。
     */
    List<DutyRosterVO> todayList();

    /**
     * 指定科室当日白班值班医生（仅已发布/生效、换班后取实际值班人），供「此刻在岗医生」聚合并入。
     * 按值班点位所属科室（orgType=1 科室、orgId=deptId、临床责任范围）匹配，只取白班；
     * 不走全院总值班口径，也不含夜班。门诊结束后据此仍能找到替本科室兜底的值班医生。
     */
    List<DutyRosterVO> listDeptDayOnDuty(Long deptId, LocalDate date);

    /**
     * 排班分页：日期区间缺省给前后各一段窗口，可按班次、值班角色、值班人、点位或责任范围筛。
     * 用于「总值班排班」页的排班表格；科室医师的一线/二线/三线值班靠责任范围过滤从全院总值班里分离出来。
     */
    PageResult<DutyRosterVO> listPage(DutyRosterPageQueryDTO q);

    /**
     * 登记或修改排班：同一天 + 同班次 + 同角色重复提交即覆盖原值班人。
     * 排班页的新增/修改弹框保存时调用，回读整行供页面直接刷新（主键字符串化，长 id 不丢精度）。
     */
    DutyRosterVO upsert(DutyRosterUpsertDTO dto);

    /**
     * 临时换班：只记换班人与原因，原排班人保留在记录里。
     * 排班页「临时换班」弹框提交时调用，事后仍要能看出本该谁值班。
     */
    void substitute(DutySubstituteDTO dto);

    /**
     * 撤回换班：恢复原排班人。
     * 换班挑错人时的补救动作，撤回后排班页的值班人列回到原人。
     */
    void cancelSubstitute(Long id);

    /**
     * 删除排班行（物理删：唯一键不含删除标志，软删会让「同日同班次同角色」永远排不上）。
     * 排班页行删除按钮。
     */
    void deleteById(Long id);

    // ===== G-15 发布态流转（提交→审核→发布 / 调班 / 撤回）=====
    /** 提交审核：草稿 → 待审核 */
    void submit(Long id);

    /** 审核发布：待审核 → 已发布（此后才进「今日值班表/此刻谁负责」） */
    void auditPublish(Long id);

    /** 撤回：任意态 → 草稿（不再对外展示） */
    void recall(Long id);

    /** 进入调班：已发布/已变更 → 调班中（锁定，改完保存转已变更） */
    void adjustStart(Long id);

    /**
     * 月度值班工时：一人一行，按实际值班人（换班后按顶替人）归并，白班/夜班/主班/副班次数与总时长。
     */
    DutyWorkloadVO monthWorkload(DutyWorkloadDTO dto);

    /**
     * 区间规则校验：连续值班 / 值班频次 / 主班漏排。只列明细不阻断保存。
     */
    DutyCheckVO check(DutyCheckDTO dto);
}
