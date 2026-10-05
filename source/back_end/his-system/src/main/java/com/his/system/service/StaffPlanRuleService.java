package com.his.system.service;

import com.his.common.base.PageResult;
import com.his.system.dto.StaffPlanRuleQueryPageDTO;
import com.his.system.dto.StaffPlanRuleUpsertDTO;
import com.his.system.entity.BizStaffPlanRule;
import com.his.system.vo.StaffPlanRuleVO;
import com.his.system.vo.StaffShortfallVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 人力配置标准服务。
 */
public interface StaffPlanRuleService {

    PageResult<StaffPlanRuleVO> pageVO(StaffPlanRuleQueryPageDTO dto);

    /**
     * 新增/修改标准（同单元 × 同班次 × 同岗位类别只允许一条）。
     */
    Long upsert(StaffPlanRuleUpsertDTO dto);

    /**
     * 某个排班单元的全部标准行（跨模块维护人力标准时用：护理排班页维护的是「病区 × 护理岗」这一批）。
     *
     * @param staffType 岗位类别，null=不限
     */
    List<BizStaffPlanRule> listByUnit(Integer orgType, Long orgId, Integer staffType);

    /** 取一条标准（不存在报错）：跨模块调用方要按 id 做权限归属判定。 */
    BizStaffPlanRule requireById(Long id);

    /**
     * 删除标准（物理删）。
     */
    void deleteById(Long id);

    /**
     * 排班写入口的人力闸门：按当前事实重算该单元该日该班该岗位类别的在岗人数，与标准比对。
     *
     * <p><b>低于最低在岗抛业务异常</b>（这个班没人接是事实错误，必须拦住）；
     * <b>高于上限只返回提示文案</b>（人力富余不是错误，拦保存等于逼排班员删掉一个能顶班的人）。
     *
     * <p><b>下限只在「净减员」时拦</b>（{@code enforceMin=false} 则只给上限提示）：
     * 排班是一班一班排出来的，空表上排第一个人时人数必然低于下限 —— 那时候拦，排班员永远排不出第一版。
     * 下限管的应该是「别把已经排好的班拆空」，不是「还没排够不许存」。
     *
     * @param enforceMin 本次操作是否让人数净减少（删除 / 改成休息请假停班 / 换班走人）
     * @return 提示文案，null=无提示
     */
    String reviewAfterChange(Integer orgType, Long orgId, Long shiftId, Integer staffType, LocalDate date,
                             boolean enforceMin);

    /**
     * 排班写入口的**人维度**闸门：工时上限、连续夜班、连续上班、班后最短休息。
     *
     * <p>{@link #reviewAfterChange} 管的是「这个单元这个班够不够人」，这里管的是
     * 「同一个人是不是被排得太狠」。两者都是同一条标准行上的字段，缺一个这套标准就是假的。
     *
     * <p><b>两类事的处理强度故意不一样</b>：
     * <ul>
     *   <li><b>劳动安全类拦</b>（下夜班没休息够就排下一班、连续夜班超上限）：
     *       人已经排上去了才会造成真实伤害，事后补班/调班都换不回来，必须当场挡住；</li>
     *   <li><b>总量类只提示</b>（单周工时、连续上班天数）：这类超标往往是「没人了只能这么排」的结果，
     *       挡下来只会让班表排不下去（护理页原本也是告警不是拦截），由提示文案让人自己判断。</li>
     * </ul>
     *
     * <p><b>只校验涉及本次这条事实的组合</b>：库里可能已经躺着历史积压的违规排班，
     * 若把所有连段都判一遍，排班员会被前人的班卡住、什么都改不动。新人新规矩。
     *
     * @param currentScheduleId 本次刚写入的那条事实ID（不在与之相关的组合上时一律不判）
     * @param employeeId        被排的这个人
     * @param staffType         岗位类别
     * @return 提示文案（总量类超标），null=无提示
     */
    String reviewEmployee(Long currentScheduleId, Long employeeId, Integer orgType, Long orgId,
                          Integer staffType, LocalDate date);

    /**
     * 时间窗内的人力缺口清单（只报告，不拦截）：启用中、最低在岗 &gt; 0 的标准行 × 窗口内每一天，
     * 实际在岗人次低于 min_staff 的组合。总览驾驶舱用。
     */
    List<StaffShortfallVO> listShortfalls(LocalDate begin, LocalDate end);
}
