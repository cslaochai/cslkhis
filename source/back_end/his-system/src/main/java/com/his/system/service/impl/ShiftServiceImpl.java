package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.*;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import com.his.system.dto.ShiftQueryPageDTO;
import com.his.system.dto.ShiftUpsertDTO;
import com.his.system.entity.BizShift;
import com.his.system.mapper.BizShiftMapper;
import com.his.system.service.ShiftService;
import com.his.system.vo.ShiftSelectListVO;
import com.his.system.vo.ShiftVO;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 班次字典服务实现
 */
@Service
public class ShiftServiceImpl extends ServiceImpl<BizShiftMapper, BizShift> implements ShiftService {

    @Override
    public List<BizShift> listShifts(Long deptId, Integer status, Integer useScope) {
        LambdaQueryWrapper<BizShift> wrapper = new LambdaQueryWrapper<>();
        if (deptId != null) {
            // 适用科室 + 全院通用（dept_id IS NULL）
            wrapper.and(w -> w.eq(BizShift::getDeptId, deptId).or().isNull(BizShift::getDeptId));
        }
        wrapper.eq(status != null, BizShift::getStatus, status);
        // 存量班次 use_scope 全为 1，历史行在 sql/166 里已回填；不传=两册都列（管理页需要）
        wrapper.eq(useScope != null, BizShift::getUseScope, useScope);
        // 二级键 id：同 start_time 的行顺序不稳定（排序铁律）
        wrapper.orderByAsc(BizShift::getStartTime).orderByAsc(BizShift::getId);
        return list(wrapper);
    }

    @Override
    public List<ShiftSelectListVO> selectListVO(Long deptId, Integer status, Integer useScope) {
        return listShifts(deptId, status, useScope).stream().map(this::toSelectVO).toList();
    }

    @Override
    public PageResult<ShiftVO> pageVO(ShiftQueryPageDTO dto) {
        LambdaQueryWrapper<BizShift> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(TextUtil.hasText(dto.getKeyword()), BizShift::getShiftName, dto.getKeyword())
                .eq(dto.getStatus() != null, BizShift::getStatus, dto.getStatus())
                .eq(dto.getUseScope() != null, BizShift::getUseScope, dto.getUseScope())
                // 二级键 id：同 start_time 的行顺序不稳定（分页铁律，防翻页重复+丢行）
                .orderByAsc(BizShift::getStartTime).orderByAsc(BizShift::getId);
        Page<BizShift> page = this.page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        List<ShiftVO> voList = page.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public void upsertShift(ShiftUpsertDTO dto) {
        BizShift shift = new BizShift();
        shift.setId(dto.getId());
        shift.setShiftName(dto.getShiftName());
        shift.setStartTime(dto.getStartTime());
        shift.setEndTime(dto.getEndTime());
        shift.setCrossDay(dto.getCrossDay());
        shift.setDeptId(dto.getDeptId());
        shift.setScheduleType(dto.getScheduleType());
        shift.setUseScope(dto.getUseScope());
        shift.setApplyStaffType(dto.getApplyStaffType());
        // 夜班与岗后休息是班次自己声明的属性，不收陌生人Guessed的起止时间：
        // 省得前端猜，也省得「晚间门诊」被当成夜班。没传就按白班/不限制落。
        shift.setIsNight(dto.getIsNight() == null ? YesOrNoEnum.NO.getCode() : dto.getIsNight());
        shift.setNeedRestHours(dto.getNeedRestHours() == null
                ? java.math.BigDecimal.ZERO : dto.getNeedRestHours());
        shift.setStatus(dto.getStatus());
        shift.setRemark(dto.getRemark());
        String error = saveShift(shift);
        if (error != null) {
            throw new BusinessException(error);
        }
    }

    private ShiftVO toVO(BizShift s) {
        if (s == null) {
            return null;
        }
        ShiftVO vo = new ShiftVO();
        vo.setId(s.getId());
        vo.setShiftName(s.getShiftName());
        vo.setStartTime(s.getStartTime());
        vo.setEndTime(s.getEndTime());
        vo.setCrossDay(s.getCrossDay());
        vo.setDurationMinutes(s.getDurationMinutes());
        vo.setDeptId(s.getDeptId());
        vo.setScheduleType(s.getScheduleType());
        vo.setUseScope(s.getUseScope());
        vo.setApplyStaffType(s.getApplyStaffType());
        vo.setIsNight(s.getIsNight());
        vo.setNeedRestHours(s.getNeedRestHours());
        vo.setStatus(s.getStatus());
        vo.setRemark(s.getRemark());
        return vo;
    }

    /**
     * 下拉只出「认名字 + 带出上下班时间」四列，不借道 {@link #toVO} 把班次的全套规定（跨度分钟、
     * 是否夜班、休息时长门槛…）先摊开再丢掉。
     */
    private ShiftSelectListVO toSelectVO(BizShift s) {
        ShiftSelectListVO vo = new ShiftSelectListVO();
        vo.setId(s.getId());
        vo.setShiftName(s.getShiftName());
        vo.setStartTime(s.getStartTime());
        vo.setEndTime(s.getEndTime());
        return vo;
    }

    @Override
    public Map<Long, BizShift> mapByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        // listByIds 会自动过滤逻辑删行：班次被删过 → 这里就没有该 id，调用方按"未分类"渲染，不猜
        return listByIds(ids).stream().collect(Collectors.toMap(BizShift::getId, s -> s));
    }

    @Override
    public Integer scheduleTypeOf(Long shiftId) {
        if (shiftId == null) {
            return null;
        }
        BizShift shift = getById(shiftId);
        return shift == null ? null : shift.getScheduleType();
    }

    @Override
    public BizShift resolveForScheduling(Long shiftId, Long deptId) {
        return resolveForScheduling(shiftId, deptId, null);
    }

    @Override
    public BizShift resolveForScheduling(Long shiftId, Long deptId, Integer staffType) {
        // C-非 web 入参：由排班/模板的 service 用实体字段直接调用，不经请求体绑定，Bean Validation 不覆盖，保留
        if (shiftId == null) {
            throw new BusinessException("请选择班次：排班的时间段与班别都由班次带出，不够用到「班次字典」先建一条");
        }
        BizShift shift = getById(shiftId);
        if (shift == null) {
            throw new BusinessException("所选班次不存在或已删除，请重新选择");
        }
        if (shift.getStatus() != null && shift.getStatus() == 0) {
            throw new BusinessException("班次「" + shift.getShiftName() + "」已停用，不能用于排班");
        }
        // 适用域收口：护理班次（后夜班 00:00~08:00 这类）排进门诊等于给患者约一个没有医生的时段
        if (!ShiftUseScopeEnum.usableByOutpatient(shift.getUseScope())) {
            throw new BusinessException("班次「" + shift.getShiftName() + "」是病区护理班次，不能用于门诊排班");
        }
        if (shift.getDeptId() != null && !shift.getDeptId().equals(deptId)) {
            throw new BusinessException("班次「" + shift.getShiftName() + "」不适用于本科室");
        }
        if (shift.getScheduleType() == null || ScheduleTypeEnum.fromCode(shift.getScheduleType()) == null) {
            throw new BusinessException("班次「" + shift.getShiftName() + "」未配置班别（"
                    + ScheduleTypeEnum.whitelistText() + "），请先到班次字典补全");
        }
        // 岗位适用：班次没配适用岗位=全院通用；配了就只能排给该类别的岗位
        // （「夜诊听班」这类值守班次配 1-医生，收费员选中它排班就是排一个没人出的班）
        if (staffType != null && shift.getApplyStaffType() != null
                && !shift.getApplyStaffType().equals(staffType)) {
            throw new BusinessException("班次「" + shift.getShiftName() + "」是"
                    + StaffTypeEnum.getText(shift.getApplyStaffType()) + "适用的班次，不能排给"
                    + StaffTypeEnum.getText(staffType) + "岗位");
        }
        return shift;
    }

    @Override
    public String saveShift(BizShift shift) {
        // 1) 时间格式
        LocalTime start;
        LocalTime end;
        try {
            start = LocalTime.parse(shift.getStartTime(), DateFormats.TIME_MINUTE);
            end = LocalTime.parse(shift.getEndTime(), DateFormats.TIME_MINUTE);
        } catch (Exception e) {
            return "时间格式必须为 HH:mm（如 08:00）";
        }
        // 2) 班次类型只允许已知码值
        if (shift.getScheduleType() != null && ScheduleTypeEnum.fromCode(shift.getScheduleType()) == null) {
            return "班次类型只允许 " + ScheduleTypeEnum.whitelistText();
        }
        // 3) 适用域：只认已知码值，空=门诊排班（护理班次由病区护理排班页维护）
        //    必须先定册再判跨零点——闸门是按册放行的
        if (shift.getUseScope() != null && ShiftUseScopeEnum.fromCode(shift.getUseScope()) == null) {
            return "适用域只允许 " + ShiftUseScopeEnum.whitelistText();
        }
        if (shift.getUseScope() == null) {
            shift.setUseScope(ShiftUseScopeEnum.OUTPATIENT.getCode());
        }
        // 4) 跨零点闸门按册放行 + 时长强制重算：「用于校验」的落地——前端传什么都不认
        if (start.isBefore(end)) {
            shift.setCrossDay(YesOrNoEnum.NO.getCode());
            shift.setDurationMinutes((int) Duration.between(start, end).toMinutes());
        } else {
            // 起止相等按「整天」处理（00:00~00:00 这类 24 小时值守班）
            if (!ShiftUseScopeEnum.allowsCrossDay(shift.getUseScope())) {
                return "结束时间必须晚于开始时间（只有全院值守/全院通用册允许跨零点班次）";
            }
            shift.setCrossDay(YesOrNoEnum.YES.getCode());
            // LocalTime 相减不回绕：18:00 到零点用 Duration.between(18:00, 00:00) 得到的是 -1080 分钟，
            // 所以「开始到次日零点」这段要用整天数减去「零点到开始」，否则会写出负时长
            shift.setDurationMinutes((int) (Duration.ofDays(1)
                    .minus(Duration.between(LocalTime.MIDNIGHT, start))
                    .plus(Duration.between(LocalTime.MIDNIGHT, end))
                    .toMinutes()));
        }
        // 5) 同名同科室防重（@TableLogic 自动过滤已删行；dept_id 为 NULL 时按 IS NULL 匹配）
        LambdaQueryWrapper<BizShift> dup = new LambdaQueryWrapper<>();
        dup.eq(BizShift::getShiftName, shift.getShiftName());
        if (shift.getDeptId() != null) {
            dup.eq(BizShift::getDeptId, shift.getDeptId());
        } else {
            dup.isNull(BizShift::getDeptId);
        }
        dup.ne(shift.getId() != null, BizShift::getId, shift.getId());
        if (count(dup) > 0) {
            return "同名班次已存在（同科室范围内）";
        }
        // 6) 状态缺省启用
        if (shift.getStatus() == null) {
            shift.setStatus(EnableStatusEnum.ENABLED.getCode());
        }
        return saveOrUpdate(shift) ? null : "保存失败";
    }

    @Override
    public boolean deleteShift(Long id) {
        return removeById(id);
    }

    @Override
    public String renameShift(Long id, String shiftName) {
        // C类（非请求体入参）：该接口按表单参数传入，不经请求体绑定，注解拦不到空白串
        if (shiftName == null || shiftName.trim().isEmpty()) {
            return "班次名称不能为空";
        }
        String name = shiftName.trim();
        BizShift exist = getById(id);
        if (exist == null) {
            return "班次不存在或已删除";
        }
        if (name.equals(exist.getShiftName())) {
            // 名字没变，幂等成功
            return null;
        }
        // 同名同科室防重（排除自身）；口径与 saveShift 一致
        LambdaQueryWrapper<BizShift> dup = new LambdaQueryWrapper<>();
        dup.eq(BizShift::getShiftName, name);
        if (exist.getDeptId() != null) {
            dup.eq(BizShift::getDeptId, exist.getDeptId());
        } else {
            dup.isNull(BizShift::getDeptId);
        }
        dup.ne(BizShift::getId, id);
        if (count(dup) > 0) {
            return "同名班次已存在（同科室范围内）";
        }
        // 只更新 shift_name 一列：时间/科室/类型/状态不可经此接口改动（维护界面的后端边界）
        return lambdaUpdate().set(BizShift::getShiftName, name).eq(BizShift::getId, id).update() ? null : "改名失败";
    }

    @Override
    public String updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            return "状态只允许 1-启用 / 0-停用";
        }
        BizShift exist = getById(id);
        if (exist == null) {
            return "班次不存在或已删除";
        }
        if (status.equals(exist.getStatus())) {
            // 状态没变，幂等成功
            return null;
        }
        // 只更新 status 一列：名称/时间/科室/类型不可经此接口改动
        return lambdaUpdate().set(BizShift::getStatus, status).eq(BizShift::getId, id).update() ? null : "状态更新失败";
    }
}
