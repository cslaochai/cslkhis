package com.his.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.system.dto.ShiftQueryPageDTO;
import com.his.system.dto.ShiftUpsertDTO;
import com.his.system.entity.BizShift;
import com.his.system.vo.ShiftSelectListVO;
import com.his.system.vo.ShiftVO;
import com.his.common.base.PageResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 班次字典服务
 */
public interface ShiftService extends IService<BizShift> {

    /**
     * 班次列表（下拉/管理共用）：deptId 传了 = 该科室适用 + 全院通用；status 可选过滤。
     * useScope 传了 = 只列该册（1-门诊排班 2-病区护理），护理班次不会串进门诊下拉。
     * 按 start_time 升序，id 二级键兜底同刻行序。
     */
    List<BizShift> listShifts(Long deptId, Integer status, Integer useScope);

    /**
     * 班次下拉出参（口径同 {@link #listShifts}）。
     */
    List<ShiftSelectListVO> selectListVO(Long deptId, Integer status, Integer useScope);

    /**
     * 班次分页出参：关键词=名称模糊，排序 开始时间 + id 二级键。
     */
    PageResult<ShiftVO> pageVO(ShiftQueryPageDTO dto);

    /**
     * 新增/修改班次（入参即前端表单）：校验不通过按服务层文案抛业务异常。
     */
    void upsertShift(ShiftUpsertDTO dto);

    /**
     * 排班/排班模板写入口的班次解析——**班次是时间段与班别的唯一来源**，前端传什么都不认。
     * 不限制岗位类别，口径同 {@link #resolveForScheduling(Long, Long, Integer)} 传空。
     *
     * @return 班次行（调用方从中取 startTime/endTime/scheduleType）
     */
    BizShift resolveForScheduling(Long shiftId, Long deptId);

    /**
     * 排班/排班模板写入口的班次解析，追加<b>岗位适用</b>闸门。
     *
     * <p>校验链（每一步都给出可执行的报错，不静默兜底）：
     * 必须选班次 → 班次存在 → 班次启用 → <b>适用域可被门诊排班选走</b>（病区护理班次不许排进门诊）
     * → 适用于该科室（科室为空视为全院通用）→ 班别已配置 → <b>岗位类别相符</b>
     * （班次配了适用岗位类别时，只能排给该类别的岗位；未配=全部岗位通用）。
     *
     * <p>「班别未配置」那条是"班次字典必须全"的落地点：字典行没配排班类型时
     * <b>不</b>按起止时间猜一个班别（「全天门诊 08:00~17:00」和「半日班 08:00~12:00」猜出来会串），
     * 而是要求到班次字典补全。
     *
     * @param staffType 本次排班的岗位类别（StaffTypeEnum 码值，空=不校验岗位适用）
     * @return 班次行（调用方从中取 startTime/endTime/scheduleType）
     */
    BizShift resolveForScheduling(Long shiftId, Long deptId, Integer staffType);

    /**
     * 批量取班次（渲染用）：给排班列表/看板一次性补班次名与班别，避免每行一次查询。
     * 空入参与 NULL key 都返回空 Map（{@code listByIds} 对空集合会抛异常）。
     */
    Map<Long, BizShift> mapByIds(Collection<Long> ids);

    /**
     * 单个班次的班别（挂号/预约单上的 schedule_type 快照靠它取，排班表本身已不存班别）。
     * 班次没选或已被删 → null，不猜。
     */
    Integer scheduleTypeOf(Long shiftId);

    /**
     * 新增/修改班次。
     * 校验：时间格式 HH:mm、同日起止时结束晚于开始（跨零点只放行给全院值守/全院通用册，
     * 并按册回写跨零点标记）、同名同科室防重；
     * duration_minutes 强制按起止时间重算（前端传值不认）；
     * useScope 只允许已知码值，空=1-门诊排班（门诊页建的班次不许漂进护理册）。
     *
     * @return 错误文案，null=成功
     */
    String saveShift(BizShift shift);

    /**
     * 删除班次（逻辑删）。
     */
    boolean deleteShift(Long id);

    /**
     * 班次改名——维护界面唯一入口：**只更新 shift_name 一列**，
     * 起止时间/时长/科室/类型/状态不可经此接口改动。
     * 校验：名字非空、班次存在、同名同科室防重（排除自身）。
     *
     * @return 错误文案，null=成功
     */
    String renameShift(Long id, String shiftName);

    /**
     * 班次启用/停用——维护界面入口：**只更新 status 一列**。
     * 校验：status 仅允许 0-停用 / 1-启用、班次存在；同状态幂等成功。
     *
     * @return 错误文案，null=成功
     */
    String updateStatus(Long id, Integer status);
}
